onedev.server.buildSteps = {
    init: function(id, callback, resume, maxEntries, latestEntriesNotice, noEntriesNotice, pausedText, resumeText, noStepsNotice) {
        const self = this;
        if (self.timer) clearInterval(self.timer);
        self.stopPositioning();
        if (self.steps) self.steps.forEach(step => {
            if (step.stepPositionTooltip) step.stepPositionTooltip.destroy();
            if (step.statusTooltip) step.statusTooltip.destroy();
            step.downloadTooltip.destroy();
        });
        self.container = document.getElementById(id);
        self.layout = self.container.closest('.build-steps-layout');
        self.progressElement = self.layout.querySelector('.build-duration-progress');
        self.positionListener = function() {
            if (!self.positionFrame)
                self.positionFrame = requestAnimationFrame(function() {
                    self.positionFrame = undefined;
                    if (self.container.isConnected) self.updateStickyTop();
                });
        };
        window.addEventListener('scroll', self.positionListener, true);
        window.addEventListener('resize', self.positionListener);
        self.positionObserver = new ResizeObserver(self.positionListener);
        self.positionObserver.observe(self.layout);
        self.positionObserver.observe(self.container);
        const tabs = $(self.layout).siblings('.sticky-tabs-row')[0];
        if (tabs) self.positionObserver.observe(tabs);
        self.callback = callback;
        self.resume = resume;
        self.maxEntries = maxEntries;
        self.latestEntriesNotice = latestEntriesNotice;
        self.noEntriesNotice = noEntriesNotice;
        self.pausedText = pausedText;
        self.resumeText = resumeText;
        self.noStepsNotice = noStepsNotice;
        self.steps = new Map();
        self.sequence = undefined;
        self.busy = false;
        self.queued = false;
        self.autoUpdate = true;
        self.timer = setInterval(function() {
            if (!self.container.isConnected) {
                clearInterval(self.timer);
                self.stopPositioning();
                return;
            }
            if (self.autoUpdate)
                self.steps.forEach(step => self.showDuration(step));
        }, 1000);
    },
    stopPositioning: function() {
        if (this.positionListener) {
            window.removeEventListener('scroll', this.positionListener, true);
            window.removeEventListener('resize', this.positionListener);
        }
        if (this.positionObserver) this.positionObserver.disconnect();
        if (this.positionFrame) cancelAnimationFrame(this.positionFrame);
        this.positionFrame = undefined;
    },
    setAutoUpdate: function(enabled) {
        this.autoUpdate = enabled;
        this.busy = false;
        this.queued = false;
        if (enabled)
            this.steps.forEach(step => { step.fetch = step.expanded; });
    },
    setProgress: function(progress) {
        if (!this.autoUpdate) return;
        onedev.server.timeProgress.update(this.progressElement.id,
            progress ? progress.elapsed / 1000 : 0, progress ? progress.expected / 1000 : 0);
        this.updateStickyTop();
    },
    getOffsets: function() {
        const offsets = {};
        this.steps.forEach(step => {
            if (step.expanded && (this.autoUpdate || step.fetch)) offsets[step.name] = step.next;
        });
        return JSON.stringify(offsets);
    },
    refresh: function() {
        if (!this.container || !this.container.isConnected) return;
        if (!this.autoUpdate && !Array.from(this.steps.values()).some(step => step.expanded && step.fetch)) return;
        if (this.busy) {
            this.queued = true;
            return;
        }
        this.busy = true;
        this.callback(this.getOffsets());
    },
    showDuration: function(step) {
        if (!step.durationLabel) return;
        const seconds = Math.floor((step.duration + (step.status === 'RUNNING' ? Date.now() - step.updated : 0)) / 1000);
        step.durationLabel.text(Math.floor(seconds / 60) + 'm ' + (seconds % 60) + 's');
    },
    setExpanded: function(step, expanded) {
        step.expanded = expanded;
        step.section.toggleClass('step-collapsed', !expanded);
        step.body.toggle(expanded);
        step.toggle.attr('aria-expanded', expanded);
        step.arrow.toggleClass('rotate-90', expanded);
        if (!expanded) {
            step.fetch = false;
            step.log.empty();
            step.next = 0;
            step.notice.hide();
        }
    },
    create: function(data) {
        const self = this;
        const step = {
            name: data.name,
            next: 0,
            expanded: data.active
        };
        step.title = data.title;
        const section = $('<section class="build-step border mb-3"></section>').appendTo(self.container);
        step.section = section;
        const header = $('<div class="step-header bg-white d-flex align-items-center p-3"></div>').appendTo(section);
        step.header = header;
        step.toggle = $('<button type="button" class="step-toggle btn btn-link text-left p-0 font-weight-bold d-inline-flex align-items-center"></button>').appendTo(header);
        step.arrow = $('<svg class="icon icon-sm mr-1"><use xlink:href="' + onedev.server.icons + '#arrow"/></svg>').appendTo(step.toggle);
        $('<span></span>').text(step.title).appendTo(step.toggle);
        if (data.stepIndex != null) {
            step.stepPosition = $('<span class="step-position text-muted font-weight-normal text-nowrap ml-2" tabindex="0"></span>')
                .appendTo(header);
            step.stepPositionTooltip = tippy(step.stepPosition[0], {
                content: data.stepPositionText,
                delay: [500, 0],
                placement: 'auto'
            });
        }
        if (data.status) {
            step.statusLabel = $('<span class="step-status ml-2 d-inline-flex" role="img" tabindex="0"></span>').appendTo(header);
            step.statusIcon = $('<svg aria-hidden="true"><use/></svg>').appendTo(step.statusLabel);
            step.statusTooltip = tippy(step.statusLabel[0], {
                content: data.statusText,
                delay: [500, 0],
                placement: 'auto'
            });
            step.durationLabel = $('<span class="step-duration text-muted ml-2 d-none d-md-inline"></span>').appendTo(header);
        }
        const actions = $('<div class="step-actions d-flex align-items-center ml-auto"></div>').appendTo(header);
        step.download = $('<a class="link-secondary ml-4 d-none d-md-inline"><svg class="icon" aria-hidden="true"><use xlink:href="' + onedev.server.icons + '#download2"/></svg></a>')
            .attr('aria-label', data.downloadText).appendTo(actions);
        step.downloadTooltip = tippy(step.download[0], {
            content: data.downloadText,
            delay: [500, 0],
            placement: 'auto'
        });
        step.body = $('<div class="step-body"></div>').appendTo(section);
        step.log = $('<pre class="log text-break font-size-sm p-3 mb-0"></pre>').appendTo(step.body);
        step.notice = $('<div class="step-notice text-muted px-3 pt-3"></div>').text(self.noEntriesNotice).prependTo(step.body);
        step.paused = $('<div class="text-warning p-3"></div>').text(self.pausedText).appendTo(step.body).hide();
        if (self.resume) $('<button type="button" class="btn btn-link ml-2 p-0"></button>').text(self.resumeText).appendTo(step.paused).on('click', self.resume);
        self.setExpanded(step, step.expanded);
        step.toggle.on('click', function() {
            self.setExpanded(step, !step.expanded);
            if (step.expanded)
                step.fetch = true;
            self.refresh();
        });
        self.steps.set(step.name, step);
        return step;
    },
    update: function(data, paused, sequence, requested) {
        const self = this;
        if (!self.autoUpdate && !requested) return;
        if (self.sequence !== undefined && self.sequence !== sequence) {
            self.steps.forEach(step => {
                if (step.stepPositionTooltip) step.stepPositionTooltip.destroy();
                if (step.statusTooltip) step.statusTooltip.destroy();
                step.downloadTooltip.destroy();
            });
            self.steps.clear();
            $(self.container).empty();
        }
        self.sequence = sequence;
        if (self.autoUpdate) {
            const names = new Set(data.map(item => item.name));
            self.steps.forEach((step, name) => {
                if (!names.has(name)) {
                    if (step.stepPositionTooltip) step.stepPositionTooltip.destroy();
                    if (step.statusTooltip) step.statusTooltip.destroy();
                    step.downloadTooltip.destroy();
                    step.section.remove();
                    self.steps.delete(name);
                }
            });
        }
        data.forEach((item, index) => {
            let step = self.steps.get(item.name);
            if (!self.autoUpdate) {
                if (step && step.expanded && step.fetch)
                    self.appendEntries(step, item);
                return;
            }
            if (!step) {
                step = self.create(item);
            }
            self.updateStep(step, item, index, paused);
        });
        if (!data.length) $(self.container).text(self.noStepsNotice);
        else $(self.container).contents().filter(function() { return this.nodeType === 3; }).remove();
        self.updateStickyTop();
        if (requested) {
            self.busy = false;
            if (self.queued) {
                self.queued = false;
                self.refresh();
            }
        }
    },
    updateStep: function(step, item, index, paused) {
        const self = this;
        if (step.stepPosition)
            step.stepPosition.text(item.stepIndex + '/' + item.stepCount);
        if (step.active && !item.active)
            self.setExpanded(step, false);
        if (step.active === false && item.active)
            self.setExpanded(step, true);
        step.active = item.active;
        step.status = item.status;
        const position = self.container.children[index];
        if (position !== step.section[0]) self.container.insertBefore(step.section[0], position || null);
        if (item.status) {
            const displayStatus = item.skipped ? 'skipped' : item.status.toLowerCase();
            const borderStyle = {running: 'warning', successful: 'success', failed: 'danger', cancelled: 'danger', skipped: 'secondary'}[displayStatus];
            step.section.attr('data-status', displayStatus)
                .removeClass('border-warning border-success border-danger border-secondary').addClass('border-' + borderStyle);
            step.duration = item.duration;
            step.updated = Date.now();
            const statusIcon = {running: 'spin', successful: 'tick', failed: 'times', cancelled: 'cancel', skipped: 'minus'}[displayStatus];
            step.statusLabel.attr('aria-label', item.statusText);
            step.statusTooltip.setContent(item.statusText);
            step.statusIcon.attr('class', 'icon flex-shrink-0 ' + (item.skipped ? 'text-muted' : 'build-status-' + displayStatus)
                + (displayStatus === 'running' ? ' spin' : ''));
            step.statusIcon.find('use')[0].setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', onedev.server.icons + '#' + statusIcon);
        } else {
            step.section.addClass('build-phase border-secondary');
        }
        step.download.attr('href', item.download);
        step.paused.toggle(paused && item.status === 'RUNNING');
        self.showDuration(step);
        self.appendEntries(step, item);
    },
    appendEntries: function(step, item) {
        const self = this;
        if (item.entries && step.expanded && (self.autoUpdate || step.fetch)) {
            // A response sent before collapse may only contain a delta. Reload the tail instead.
            if (item.from > step.next) {
                self.refresh();
                return;
            }
            if (item.offset > step.next) step.log.empty();
            const newEntries = item.entries.slice(Math.max(0, step.next - item.offset));
            onedev.server.jobLogEntry.append(step.log, newEntries, true);
            const entries = step.log.children();
            step.log.toggle(entries.length > 0);
            if (entries.length > self.maxEntries) entries.slice(0, entries.length - self.maxEntries).remove();
            step.next = Math.max(step.next, item.next);
            step.fetch = false;
            step.notice.text(item.next > self.maxEntries ? self.latestEntriesNotice : self.noEntriesNotice)
                .toggleClass('pb-3', item.next === 0)
                .toggle(item.next > self.maxEntries || item.next === 0);
            if (self.autoUpdate && step.active)
                step.body[0].scrollIntoView({block: 'end'});
        }
    },
    updateStickyTop: function() {
        const layout = this.layout;
        const tabs = $(layout).siblings('.sticky-tabs-row');
        const tabsHeight = tabs.outerHeight() || 0;
        const progressHeight = this.progressElement.hidden ? 0 : this.progressElement.offsetHeight;
        layout.style.setProperty('--build-progress-sticky-top', tabsHeight + 'px');
        layout.style.setProperty('--build-step-sticky-top', (tabsHeight + progressHeight) + 'px');
        this.progressElement.classList.toggle('is-docked', !this.progressElement.hidden
            && this.progressElement.getBoundingClientRect().top > this.progressElement.parentElement.getBoundingClientRect().top + .5);
        this.steps.forEach(step => {
            const headerBounds = step.header[0].getBoundingClientRect();
            const sectionBounds = step.section[0].getBoundingClientRect();
            const docked = headerBounds.top > sectionBounds.top + .5;
            step.section.toggleClass('is-docked', docked);
            // Round the header only when it overlaps the section's bottom border.
            step.section.toggleClass('is-docked-bottom', docked
                && headerBounds.bottom >= sectionBounds.bottom - .5);
        });
        this.updateControlPosition(tabsHeight + progressHeight);
    },
    updateControlPosition: function(stepTop) {
        const control = this.layout.querySelector('.step-update-control');
        if (!control) return;
        let scrollParent = this.layout.parentElement;
        while (scrollParent && !/(auto|scroll|hidden|overlay)/.test(getComputedStyle(scrollParent).overflowY))
            scrollParent = scrollParent.parentElement;
        let scrollTop = 0;
        let visibleTop = 0;
        let visibleBottom = window.innerHeight;
        if (scrollParent && scrollParent !== document.body && scrollParent !== document.documentElement) {
            scrollTop = scrollParent.getBoundingClientRect().top + scrollParent.clientTop;
            visibleTop = Math.max(visibleTop, scrollTop);
            visibleBottom = Math.min(visibleBottom, scrollTop + scrollParent.clientHeight);
        }
        const bounds = this.container.getBoundingClientRect();
        visibleTop = Math.max(visibleTop, bounds.top, scrollTop + stepTop);
        visibleBottom = Math.min(visibleBottom, bounds.bottom);
        const top = Math.max(visibleTop, (visibleTop + visibleBottom - control.offsetHeight) / 2);
        this.layout.style.setProperty('--build-update-sticky-top', (top - scrollTop) + 'px');
    }
};
