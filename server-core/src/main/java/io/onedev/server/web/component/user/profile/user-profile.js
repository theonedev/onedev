onedev.server.userProfile = {
    onDomReady: function(activityStatsByDay, fromDay, toDay, activityStatsByType, types, translations) {
        const graphContainer = $(".user-profile .daily-stats")[0];
        const DAY_IN_MS = 24 * 60 * 60 * 1000;
        const monthNames = [translations["jan"], translations["feb"], translations["mar"], translations["apr"], translations["may"], translations["jun"], translations["jul"], translations["aug"], translations["sep"], translations["oct"], translations["nov"], translations["dec"]];

        function getEpochDay(date) {
            // Get the date at midnight in local timezone
            const localDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
            // Get the timezone offset in minutes
            const tzOffset = localDate.getTimezoneOffset();
            // Adjust for timezone to get UTC midnight
            const utcDate = new Date(localDate.getTime() - (tzOffset * 60 * 1000));
            // Convert to days since epoch
            return Math.floor(utcDate.getTime() / DAY_IN_MS);
        }

        function getDateFromEpochDay(epochDay) {
            // Convert epoch day to UTC midnight
            const utcMs = epochDay * DAY_IN_MS;
            // Create date at UTC midnight
            const utcDate = new Date(utcMs);
            // Get the timezone offset in minutes
            const tzOffset = utcDate.getTimezoneOffset();
            // Adjust for timezone to get local midnight
            const localDate = new Date(utcMs + (tzOffset * 60 * 1000));
            // Create a clean date at local midnight
            return new Date(localDate.getFullYear(), localDate.getMonth(), localDate.getDate());
        }

        const startDate = getDateFromEpochDay(fromDay);
        const endDate = getDateFromEpochDay(toDay);
        
        const activitiesMap = new Map();
        for (const dayEpochStr in activityStatsByDay) {
            activitiesMap.set(parseInt(dayEpochStr), activityStatsByDay[dayEpochStr]);
        }

        // Determine the full grid range
        let currentGridDate;
        let gridEndDate;
        const today = new Date();
        const todayEpoch = getEpochDay(today);
        
        if (startDate.getFullYear() === endDate.getFullYear()) {
            // If both dates are in same year, use start and end of that year
            currentGridDate = new Date(startDate.getFullYear(), 0, 1); // Start of year
            gridEndDate = new Date(startDate.getFullYear(), 11, 31); // End of year
        } else if (fromDay > todayEpoch - 365) {
            // If fromDay is within last year, use today and (today - 365)
            currentGridDate = new Date(today);
            currentGridDate.setDate(currentGridDate.getDate() - 365);
            gridEndDate = new Date(today);
        } else {
            // For all other cases, use toDay and (toDay - 365)
            currentGridDate = new Date(endDate);
            currentGridDate.setDate(currentGridDate.getDate() - 365);
            gridEndDate = new Date(endDate);
        }

        // Adjust to start from Sunday
        currentGridDate.setDate(currentGridDate.getDate() - currentGridDate.getDay());

        const allDaysInGrid = [];
        let tempDate = new Date(currentGridDate);
        while (tempDate <= gridEndDate) {
            allDaysInGrid.push(new Date(tempDate));
            tempDate.setDate(tempDate.getDate() + 1);
        }

        const weeksData = [];
        for (let i = 0; i < allDaysInGrid.length; i += 7) {
            weeksData.push(allDaysInGrid.slice(i, i + 7));
        }

        // Create DOM structure
        const calendarDiv = document.createElement("div");
        calendarDiv.className = "contribution-calendar";

        const monthsRow = document.createElement("div");
        monthsRow.className = "calendar-months";
        calendarDiv.appendChild(monthsRow);

        const calendarBody = document.createElement("div");
        calendarBody.className = "calendar-body";
        calendarDiv.appendChild(calendarBody);

        const dayLabelsCol = document.createElement("div");
        dayLabelsCol.className = "calendar-day-labels";
        const dayLabelTexts = [translations["sun"], translations["mon"], translations["tue"], translations["wed"], translations["thu"], translations["fri"], translations["sat"]];
        const showDayLabelFor = [false, true, false, true, false, true, false]; // Show Mon, Wed, Fri

        for (let i = 0; i < 7; i++) {
            const dayLabel = document.createElement("div");
            dayLabel.className = "day-label";
            if (showDayLabelFor[i]) {
                dayLabel.textContent = dayLabelTexts[i];
            }
            dayLabelsCol.appendChild(dayLabel);
        }
        calendarBody.appendChild(dayLabelsCol);

        const weekColumnsContainer = document.createElement("div");
        weekColumnsContainer.className = "calendar-week-columns";
        calendarBody.appendChild(weekColumnsContainer);

        // Populate week columns and day cells
        weeksData.forEach((week) => {
            const weekCol = document.createElement("div");
            weekCol.className = "calendar-week";
            week.forEach(dayDate => {
                const dayCell = document.createElement("div");
                dayCell.className = "calendar-day-cell";
                const dayEpoch = getEpochDay(dayDate);

                if (dayEpoch < fromDay || dayEpoch > toDay) {
                    dayCell.classList.add("disabled");
                } else {
                    const activityCount = activitiesMap.get(dayEpoch) || 0;
                    dayCell.setAttribute("data-date", dayDate.toISOString().split('T')[0]);
                    dayCell.setAttribute("data-count", activityCount);
                    
                    let level = 0;
                    if (activityCount > 0 && activityCount <= 2) level = 1;
                    else if (activityCount > 2 && activityCount <= 5) level = 2;
                    else if (activityCount > 5 && activityCount <= 8) level = 3;
                    else if (activityCount > 8) level = 4;
                    dayCell.setAttribute("data-level", level);

                    const dateString = `${dayDate.getFullYear()}-${String(dayDate.getMonth() + 1).padStart(2, '0')}-${String(dayDate.getDate()).padStart(2, '0')}`;
                    const tooltipText = translations["cell-tooltip"].replace("{0}", activityCount).replace("{1}", dateString);
                    dayCell.setAttribute("data-tippy-content", tooltipText);
                }
                weekCol.appendChild(dayCell);
            });
            weekColumnsContainer.appendChild(weekCol);
        });
        
        // Populate month headers
        monthsRow.innerHTML = ''; // Clear just in case
        if (weeksData.length > 0) {
            // Calculate pixel positions instead of using CSS grid
            const weekWidth = 12; // Width of each day cell
            const weekGap = 2; // Gap between weeks
            const totalWeekWidth = weekWidth + weekGap;
            const minMonthWidth = 30; // Minimum width needed to display a month label
            
            // Calculate total calendar width
            const totalCalendarWidth = (weeksData.length * totalWeekWidth - weekGap) + 30; // +25 for day labels

            monthsRow.style.position = "relative";
            monthsRow.style.height = "12px"; // Fixed height for month labels
            // Set minimum width to accommodate all weeks
            monthsRow.style.minWidth = (weeksData.length * totalWeekWidth - weekGap) + "px";

            // Set minimum width on the entire calendar to force scrolling
            calendarDiv.style.minWidth = totalCalendarWidth + "px";

            let currentLabelMonth = -1;
            let currentLabelStartWeekIndex = 0;

            for (let i = 0; i < weeksData.length; i++) {
                const firstDayOfWeek = weeksData[i][0]; // Sunday of this week
                const monthOfSunday = firstDayOfWeek.getMonth();

                if (i === 0) { // First week always starts a label
                    currentLabelMonth = monthOfSunday;
                    currentLabelStartWeekIndex = 0;
                } else if (monthOfSunday !== currentLabelMonth) { 
                    // Month changed, finalize previous label
                    const width = (i - currentLabelStartWeekIndex) * totalWeekWidth - weekGap;
                    if (width >= minMonthWidth) {
                        const monthDiv = document.createElement("div");
                        monthDiv.className = "month-label";
                        monthDiv.textContent = monthNames[currentLabelMonth];
                        
                        // Calculate pixel position and width
                        const startPos = currentLabelStartWeekIndex * totalWeekWidth;
                        
                        monthDiv.style.position = "absolute";
                        monthDiv.style.left = startPos + "px";
                        monthDiv.style.width = width + "px";
                        
                        monthsRow.appendChild(monthDiv);
                    }
                    
                    // Start new label
                    currentLabelMonth = monthOfSunday;
                    currentLabelStartWeekIndex = i;
                }

                // If it's the last week, finalize the current (or last) label
                if (i === weeksData.length - 1) {
                    const width = (weeksData.length - currentLabelStartWeekIndex) * totalWeekWidth - weekGap;
                    if (width >= minMonthWidth) {
                        const monthDiv = document.createElement("div");
                        monthDiv.className = "month-label";
                        monthDiv.textContent = monthNames[currentLabelMonth];
                        
                        // Calculate pixel position and width
                        const startPos = currentLabelStartWeekIndex * totalWeekWidth;
                        
                        monthDiv.style.position = "absolute";
                        monthDiv.style.left = startPos + "px";
                        monthDiv.style.width = width + "px";
                        
                        monthsRow.appendChild(monthDiv);
                    }
                }
            }
        }

        // Add legend
        const legendDiv = document.createElement("div");
        legendDiv.className = "contribution-legend";
        
        // Add note
        const noteDiv = document.createElement("div");
        noteDiv.className = "contribution-note";
        noteDiv.textContent = translations["user-activities-commits-note"];
        legendDiv.appendChild(noteDiv);

        // Create legend elements container
        const legendElements = document.createElement("div");
        legendElements.className = "legend-elements";

        const legendText = document.createElement("span");
        legendText.textContent = translations["less"];
        legendElements.appendChild(legendText);

        // Add color boxes
        for (let i = 0; i <= 4; i++) {
            const colorBox = document.createElement("div");
            colorBox.className = "legend-color-box";
            colorBox.setAttribute("data-level", i);
            legendElements.appendChild(colorBox);
        }

        const legendTextMore = document.createElement("span");
        legendTextMore.textContent = translations["more"];
        legendElements.appendChild(legendTextMore);

        legendDiv.appendChild(legendElements);
        calendarDiv.appendChild(legendDiv);
        graphContainer.appendChild(calendarDiv);

        tippy(calendarDiv.querySelectorAll('[data-tippy-content]'), {
            delay: [500, 0],
            placement: 'auto'
        });                        

        // Add radar chart for actual activity stats
        const typeStatsContainer = $(".user-profile .type-stats")[0];
        const chartDiv = document.createElement("div");
        chartDiv.style.width = "100%";
        chartDiv.style.height = "240px";
        typeStatsContainer.appendChild(chartDiv);
        
        // Prepare data for radar chart
        const indicatorData = types.map(type => ({
            name: type,
            value: activityStatsByType[type] || 0
        }));
        
        const maxValue = Math.max(...indicatorData.map(item => item.value), 0); // Ensure maxValue is at least 0

        const isDarkMode = onedev.server.isDarkMode();
        const axisNameColor = isDarkMode ? '#8b949e' : '#535370';
        const axisLineColor = isDarkMode ? 'rgba(255, 255, 255, 0.2)' : 'rgba(128, 128, 128, 0.2)';
        const chartColor = isDarkMode ? 'rgba(34, 134, 58, 1)' : 'rgba(64, 196, 99, 1)';
        const chartAreaColor = isDarkMode ? 'rgba(34, 134, 58, 0.2)' : 'rgba(155, 233, 168, 0.2)';

        // Create radar chart
        const chart = echarts.init(chartDiv, null, { renderer: 'canvas' }); // Explicitly use canvas renderer

        const option = {
            radar: {
                indicator: types.map(type => ({
                    name: type,
                    max: maxValue || 1 // Ensure max is at least 1 if all values are 0
                })),
                splitNumber: 4,
                axisName: {
                    color: axisNameColor,
                    fontSize: 12
                },
                splitLine: {
                    lineStyle: {
                        color: axisLineColor
                    }
                },
                splitArea: {
                    show: false
                },
                axisLine: {
                    lineStyle: {
                        color: axisLineColor
                    }
                }
            },
            series: [{
                type: 'radar',
                data: [{
                    value: indicatorData.map(item => item.value),
                    name: translations["activity-by-type"],
                    areaStyle: {
                        color: chartAreaColor
                    },
                    lineStyle: {
                        color: chartColor
                    },
                    itemStyle: {
                        color: chartColor
                    }
                }]
            }],
            tooltip: {
                trigger: 'item',
                backgroundColor: isDarkMode ? '#23232d' : 'white',
                borderColor: isDarkMode ? 'rgba(255, 255, 255, 0.2)' : 'rgba(0, 0, 0, 0.2)',
                textStyle: {
                    color: isDarkMode ? '#e6e6e6' : '#333333'
                }
            }
        };
        
        chart.setOption(option);
        
        $(typeStatsContainer).on("resized", function() {
            setTimeout(function() {
                chart.resize();
            });
        });	
    }
}