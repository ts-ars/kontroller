(function (global) {
    const font = {size: 12, family: 'Arial'};
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
    const stableSeries = (labels, values, minimumSlots = 4) => {
        const stableLabels = [...labels], stableValues = [...values];
        while (stableLabels.length < minimumSlots) { stableLabels.push(''); stableValues.push(null); }
        return {labels: stableLabels, values: stableValues};
    };
    const options = ({categorical=false, xTitle, yTitle, suggestedMax, stepSize, maxTicksLimit}={}) => ({
        responsive: true, maintainAspectRatio: false, layout: {padding: {top: 16}},
        plugins: {legend: {display: true, position: 'top', labels: {font}}, tooltip: {enabled: true}},
        scales: {
            x: {ticks: {autoSkip: !categorical, maxRotation: 0, minRotation: 0, font}, ...(xTitle ? {title:{display:true,text:xTitle}} : {})},
            y: {beginAtZero: true, ticks: {font, ...(stepSize ? {stepSize} : {}), ...(maxTicksLimit ? {maxTicksLimit} : {})},
                ...(yTitle ? {title:{display:true,text:yTitle}} : {}), ...(suggestedMax ? {suggestedMax} : {})}
        }
    });
    global.OperationalCharts = {font, positiveValueLabels, stableSeries, options};
})(window);
