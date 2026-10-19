import net from 'node:net';
import { once } from 'node:events';

// A loopback-only SMTP sink. It stores messages in memory and never relays them.
export async function startSmtpCapture(recipient) {
  const messages = [];
  const sockets = new Set();
  const server = net.createServer(socket => {
    sockets.add(socket);
    socket.on('close', () => sockets.delete(socket));
    socket.on('error', () => {});
    socket.setEncoding('latin1');
    let buffer = '';
    let data;
    socket.write('220 localhost ESMTP test capture\r\n');
    socket.on('data', chunk => {
      buffer += chunk;
      let end;
      while ((end = buffer.indexOf('\r\n')) !== -1) {
        const line = buffer.slice(0, end);
        buffer = buffer.slice(end + 2);
        if (data) {
          if (line === '.') {
            messages.push(data.join('\r\n'));
            data = undefined;
            socket.write('250 Message captured\r\n');
          } else {
            data.push(line.startsWith('..') ? line.slice(1) : line);
          }
        } else if (/^(EHLO|HELO) /i.test(line)) {
          socket.write('250 localhost\r\n');
        } else if (/^RCPT TO:/i.test(line)) {
          socket.write(line.toLowerCase().includes(`<${recipient.toLowerCase()}>`)
            ? '250 Recipient accepted\r\n' : '550 Unknown test recipient\r\n');
        } else if (/^DATA$/i.test(line)) {
          data = [];
          socket.write('354 End with a single dot\r\n');
        } else if (/^QUIT$/i.test(line)) {
          socket.end('221 Bye\r\n');
        } else if (/^(MAIL FROM:|RSET$|NOOP$)/i.test(line)) {
          socket.write('250 OK\r\n');
        } else {
          socket.write('502 Command not implemented\r\n');
        }
      }
    });
  });
  server.listen(0, '127.0.0.1');
  await once(server, 'listening');
  return {
    messages,
    connector: {
      '@type': 'SmtpImapConnector', smtpHost: '127.0.0.1',
      sslSetting: { '@type': 'SmtpWithoutSsl', port: server.address().port },
      systemAddress: 'branding@example.test', concurrency: 1, timeout: 10,
    },
    async close() {
      for (const socket of sockets) socket.destroy();
      await new Promise((resolve, reject) => server.close(error => error ? reject(error) : resolve()));
    },
  };
}

function decodeBody(body, encoding) {
  if (encoding?.toLowerCase() === 'base64') return Buffer.from(body, 'base64').toString('utf8');
  if (encoding?.toLowerCase() === 'quoted-printable') {
    body = body.replace(/=\r?\n/g, '').replace(/=([0-9a-f]{2})/gi, (_, hex) => String.fromCharCode(parseInt(hex, 16)));
  }
  return Buffer.from(body, 'latin1').toString('utf8');
}

function decodeHeader(value) {
  // RFC 2047 permits folding between adjacent encoded words; that whitespace
  // is not part of the decoded subject or sender name.
  return value.replace(/(=\?[^?]+\?[bq]\?[^?]*\?=)\s+(?==\?)/gi, '$1')
    .replace(/=\?([^?]+)\?([bq])\?([^?]*)\?=/gi, (_, charset, encoding, content) => {
      const bytes = encoding.toLowerCase() === 'b'
        ? Buffer.from(content, 'base64')
        : Buffer.from(content.replace(/_/g, ' ').replace(/=([0-9a-f]{2})/gi,
          (_, hex) => String.fromCharCode(parseInt(hex, 16))), 'latin1');
      return new TextDecoder(charset).decode(bytes);
    });
}

// Read the MIME envelope and alternative bodies produced by Jakarta Mail.
export function parseCapturedMail(raw) {
  const separator = raw.indexOf('\r\n\r\n');
  if (separator < 0) throw new Error('Captured mail has no MIME header separator');
  const headers = Object.fromEntries(raw.slice(0, separator).replace(/\r\n[ \t]+/g, ' ')
    .split('\r\n').map(line => {
      const colon = line.indexOf(':');
      return [line.slice(0, colon).toLowerCase(), decodeHeader(line.slice(colon + 1).trim())];
    }));
  const body = raw.slice(separator + 4);
  const boundary = headers['content-type']?.match(/boundary=(?:"([^"]+)"|([^;\s]+))/i);
  if (boundary) {
    const parts = body.split(`--${boundary[1] ?? boundary[2]}`).slice(1, -1)
      .map(part => parseCapturedMail(part.replace(/^\r\n/, '').replace(/\r\n$/, '')));
    return { headers, parts };
  }
  return { headers, body: decodeBody(body, headers['content-transfer-encoding']) };
}
