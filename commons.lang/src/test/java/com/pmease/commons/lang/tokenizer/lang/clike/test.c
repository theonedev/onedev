/* C demo code */

#include <zmq.h>
#include <pthread.h>
#include <semaphore.h>
#include <time.h>
#include <stdio.h>
#include <fcntl.h>
#include <malloc.h>

typedef struct {
  void* arg_socket;
  zmq_msg_t* arg_msg;
  char* arg_string;
  unsigned long arg_len;
  int arg_int, arg_command;

  int signal_fd;
  int pad;
  void* context;
  sem_t sem;
} acl_zmq_context;

#define p(X) (context->arg_##X)

void* zmq_thread(void* context_pointer) {
  acl_zmq_context* context = (acl_zmq_context*)context_pointer;
  char ok = 'K', err = 'X';
  int res;

  while (1) {
    while ((res = sem_wait(&context->sem)) == EINTR);
    if (res) {write(context->signal_fd, &err, 1); goto cleanup;}
    switch(p(command)) {
    case 0: goto cleanup;
    case 1: p(socket) = zmq_socket(context->context, p(int)); break;
    case 2: p(int) = zmq_close(p(socket)); break;
    case 3: p(int) = zmq_bind(p(socket), p(string)); break;
    case 4: p(int) = zmq_connect(p(socket), p(string)); break;
    case 5: p(int) = zmq_getsockopt(p(socket), p(int), (void*)p(string), &p(len)); break;
    case 6: p(int) = zmq_setsockopt(p(socket), p(int), (void*)p(string), p(len)); break;
    case 7: p(int) = zmq_send(p(socket), p(msg), p(int)); break;
    case 8: p(int) = zmq_recv(p(socket), p(msg), p(int)); break;
    case 9: p(int) = zmq_poll(p(socket), p(int), p(len)); break;
    }
    p(command) = errno;
    write(context->signal_fd, &ok, 1);
  }
 cleanup:
  close(context->signal_fd);
  free(context_pointer);
  return 0;
}

void* zmq_thread_init(void* zmq_context, int signal_fd) {
  acl_zmq_context* context = malloc(sizeof(acl_zmq_context));
  pthread_t thread;

  context->context = zmq_context;
  context->signal_fd = signal_fd;
  sem_init(&context->sem, 1, 0);
  pthread_create(&thread, 0, &zmq_thread, context);
  pthread_detach(thread);
  return context;
}
