(function (global) {
    const font = {size: 12, family: 'Arial'};
    const colors = {production: '#16a34a', unexplained: '#dc2626', loss: '#3b82f6'};
    const profiles = Object.freeze({
        production: {color: colors.production, label: 'Production', axis: 'time', stableSlots: 4},
        unexplained: {color: colors.unexplained, label: 'Unexplained plan difference', axis: 'time', stableSlots: 4},
        lossTypes: {color: colors.loss, label: 'Lost minutes by type', axis: 'category', stableSlots: 4, mobileRotation: 35},
        lossSensors: {color: colors.loss, label: 'Lost cans by sensor', axis: 'category', stableSlots: 4},
        lossTime: {color: colors.loss, label: 'Lost cans over time', axis: 'time', stableSlots: 4},
        planFactProduction: {
            color: colors.loss, label: 'Cans', axis: 'time', stableSlots: 0,
            xTitle: 'Time', yTitle: 'Cans', suggestedMax: 100, stepSize: 20, maxTicksLimit: 10
        }
    });

    const positiveValueLabels = {id: 'positiveValueLabels', afterDatasetsDraw(chart) {
        const {ctx} = chart, occupied = [];
        ctx.save(); ctx.font = 'bold 12px Arial'; ctx.fillStyle = '#111'; ctx.textAlign = 'center';
        chart.getDatasetMeta(0).data.forEach((bar, index) => {
            const value = Number(chart.data.datasets[0].data[index] || 0);
            if (value <= 0) return;
            const text = value.toLocaleString(), halfWidth = ctx.measureText(text).width / 2;
            const makeBox = y => ({left: bar.x-halfWidth-3, right: bar.x+halfWidth+3, top: y-13, bottom: y+3});
            let y = bar.y - 5, box = makeBox(y);
            if (occupied.some(other => box.left<other.right && box.right>other.left && box.top<other.bottom && box.bottom>other.top)) {
                y = bar.base-bar.y >= 22 ? bar.y+14 : bar.y-19; box = makeBox(y);
            }
            occupied.push(box); ctx.fillText(text, bar.x, y);
        });
        ctx.restore();
    }};

    const stableSeries = (labels, values, minimumSlots) => {
        const stableLabels = [...labels], stableValues = [...values];
        while (stableLabels.length < minimumSlots) { stableLabels.push(''); stableValues.push(null); }
        return {labels: stableLabels, values: stableValues};
    };

    const compactDateLabel = label => String(label)
        .replace(/\b(\d{4})-(\d{2})-(\d{2})\b/g, '$3.$2')
        .replace(/\b(\d{4})-(\d{2})\b/g, '$2.$1');

    const isMobile = () => global.matchMedia?.('(max-width: 600px)').matches === true;
    const optionsFor = profile => {
        const labelRotation = profile.mobileRotation && isMobile() ? profile.mobileRotation : 0;
        return {
            responsive: true,
            maintainAspectRatio: false,
            layout: {padding: {top: 16}},
            plugins: {legend: {display: true, position: 'top', labels: {font}}, tooltip: {enabled: true}},
            scales: {
                x: {
                    ticks: {autoSkip: profile.axis !== 'category', maxRotation: labelRotation, minRotation: labelRotation, font},
                    ...(profile.xTitle ? {title: {display: true, text: profile.xTitle}} : {})
                },
                y: {
                    beginAtZero: true,
                    ticks: {
                        font,
                        ...(profile.stepSize ? {stepSize: profile.stepSize} : {}),
                        ...(profile.maxTicksLimit ? {maxTicksLimit: profile.maxTicksLimit} : {})
                    },
                    ...(profile.yTitle ? {title: {display: true, text: profile.yTitle}} : {}),
                    ...(profile.suggestedMax ? {suggestedMax: profile.suggestedMax} : {})
                }
            }
        };
    };

    const charts = new Set();
    const create = (target, role, labels, values, overrides = {}) => {
        const profile = profiles[role];
        if (!profile) throw new Error(`Unknown operational chart role: ${role}`);
        const renderedLabels = profile.axis === 'time' ? labels.map(compactDateLabel) : [...labels];
        const series = stableSeries(renderedLabels, values, profile.stableSlots);
        const chart = new global.Chart(target, {
            type: 'bar',
            data: {labels: series.labels, datasets: [{label: overrides.label || profile.label, data: series.values, backgroundColor: profile.color}]},
            options: optionsFor(profile),
            plugins: [positiveValueLabels]
        });
        charts.add({chart, profile});
        return chart;
    };

    global.matchMedia?.('(max-width: 600px)').addEventListener?.('change', () => {
        charts.forEach(({chart, profile}) => {
            const rotation = profile.mobileRotation && isMobile() ? profile.mobileRotation : 0;
            chart.options.scales.x.ticks.minRotation = rotation;
            chart.options.scales.x.ticks.maxRotation = rotation;
            chart.update('none');
        });
    });

    global.OperationalCharts = {font, profiles, positiveValueLabels, stableSeries, compactDateLabel, create};
})(window);
