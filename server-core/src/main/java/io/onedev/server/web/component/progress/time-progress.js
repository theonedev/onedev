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
        if (element.hidden) return;
        this.render(element);
        state.timer = setInterval(() => {
            if (!element.isConnected) {
                clearInterval(state.timer);
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
    }
};
