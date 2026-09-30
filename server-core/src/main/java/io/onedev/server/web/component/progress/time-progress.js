onedev.server.timeProgress = {
    onDomReady: function(id) {
        const element = document.getElementById(id);
        // A containing page may have already supplied a fresher timing snapshot.
        if (element && !element.timeProgress)
            this.update(id, Number(element.dataset.elapsed), Number(element.dataset.estimatedDuration));
    },
    update: function(id, elapsed, estimatedDuration) {
        const element = document.getElementById(id);
        if (!element) return;
        const previous = element.timeProgress;
        if (previous) clearInterval(previous.timer);
        const state = {elapsed: Math.max(0, elapsed), estimatedDuration: estimatedDuration, updated: performance.now()};
        element.timeProgress = state;
        element.hidden = !Number.isFinite(estimatedDuration) || estimatedDuration <= 0;
        if (element.hidden) {
            if (element._tippy) element._tippy.hide();
            return;
        }
        this.render(element);
        state.timer = setInterval(() => {
            if (!element.isConnected) {
                clearInterval(state.timer);
                if (element._tippy) element._tippy.destroy();
                return;
            }
            this.render(element);
        }, 1000);
    },
    render: function(element) {
        const state = element.timeProgress;
        const elapsed = state.elapsed + (performance.now() - state.updated) / 1000;
        // A duration estimate cannot establish that the operation is complete.
        const percent = Math.min(99, Math.max(0, elapsed / state.estimatedDuration * 100));
        element.querySelector('.time-progress-fill').style.width = percent + '%';
        element.setAttribute('aria-valuenow', Math.round(percent));
        const content = document.createElement('div');
        const elapsedLine = document.createElement('div');
        elapsedLine.textContent = element.dataset.elapsedTemplate.replace('{0}', this.formatDuration(element, elapsed));
        content.appendChild(elapsedLine);
        const remainingLine = document.createElement('div');
        remainingLine.textContent = element.dataset.remainingTemplate.replace('{0}',
            this.formatDuration(element, Math.max(0, state.estimatedDuration - elapsed)));
        content.appendChild(remainingLine);
        if (element._tippy) {
            element._tippy.setContent(content);
        } else {
            tippy(element, {content: content, delay: [500, 0], placement: 'auto'});
        }
    },
    formatDuration: function(element, duration) {
        const seconds = Math.floor(duration);
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);
        if (hours > 0)
            return element.dataset.hoursTemplate.replace('{0}', hours).replace('{1}', minutes % 60).replace('{2}', seconds % 60);
        if (minutes > 0)
            return element.dataset.minutesTemplate.replace('{0}', minutes).replace('{1}', seconds % 60);
        return element.dataset.secondsTemplate.replace('{0}', seconds);
    }
};
