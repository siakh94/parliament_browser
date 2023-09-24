const chart = {};
chart.margin = { top: 10, right: 30, bottom: 60, left: 60 };
chart.width = 1000 - chart.margin.left - chart.margin.right;
chart.height = 400 - chart.margin.top - chart.margin.bottom;

const f = (x) => x**2;

const testData = Array.from({ length: 20 }, (_, i) => ({x:i,y:f(i)}));

/**
 * 
 * @param {string} selector querySelector parameter.
 * @param {{ x: string | number, y: string | number }[]} data Data to visualize.
 * @param {bool} xnumeric Whether the x-axis is comprised of numerical values. Defaults to false.
 */
chart.line = function(selector, data, xnumeric = false) {
    const svg = d3.select(selector)
        .append("svg")
        .attr("width", chart.width + chart.margin.left + chart.margin.right)
        .attr("height", chart.height + chart.margin.top + chart.margin.bottom)
        .append("g")
        .attr("transform", "translate(" + chart.margin.left + "," + chart.margin.top + ")");

    const x = xnumeric ? d3.scaleLinear().range([0, chart.width]) : d3.scaleBand().range([0, chart.width]);
    const y = d3.scaleLinear()
        .range([chart.height, 0]);

    if (xnumeric)
        x.domain([d3.min(data, d => d.x), d3.max(data, d => d.x)]);
    else
        x.domain(data.map(d => d.x));
    y.domain([Math.min(d3.min(data, d => d.y), 0), d3.max(data, d => d.y)]);

    svg.append("g").call(d3.axisLeft(y));
    svg.append("g").attr("transform", `translate(0, ${chart.height})`).call(d3.axisBottom(x))
        .selectAll("text")
        .attr("transform", "translate(-10,0)rotate(-30)")
        .style("text-anchor", "end");

    svg.append("path")
        .datum(data)
        .attr("fill", "none")
        .attr("stroke", "steelblue")
        .attr("stroke-width", 1.5)
        .attr("d", d3.line()
            .x(d => x(d.x))
            .y(d => y(d.y))
        );
}

/**
 * Draws a bar chart visualizing data.
 * @param {string} selector querySelector parameter.
 * @param {{ x: string | number, y: string | number }[]} data Data to visualize.
 * @param {(...args: any) => string} hoverfn HTML string to be inserted into tooltip.
 */
chart.bar = function(selector, data, hoverfn) {
    const svg = d3.select(selector)
        .append("svg")
        .attr("width", chart.width + chart.margin.left + chart.margin.right)
        .attr("height", chart.height + chart.margin.top + chart.margin.bottom)
        .append("g")
        .attr("transform", "translate(" + chart.margin.left + "," + chart.margin.top + ")");

    const x = d3.scaleBand()
        .range([0, chart.width]);
    const y = d3.scaleLinear()
        .range([chart.height, 0]);

    x.domain(data.map(d => d.x)).padding(0.2);
    y.domain([Math.min(d3.min(data, d => d.y), 0), d3.max(data, d => d.y)]);

    svg.append("g").call(d3.axisLeft(y));
    svg.append("g").attr("transform", `translate(0, ${chart.height})`).call(d3.axisBottom(x))
        .selectAll("text")
        .attr("transform", "translate(-10,0)rotate(-30)")
        .style("text-anchor", "end");
    
    const tooltip = d3.select(selector).append("div")
        .attr("class", "tooltip card border-left-primary shadow py-2")
        .style("opacity", 0)
        .style("position", "absolute")
        .style("z-index", 1000);

    svg.selectAll(".bar")
        .data(data)
        .enter()
        .append("rect")
            // .attr("class", "rectArea")
            .attr("x", d => x(d.x))
            .attr("y", d => y(d.y))
            .attr("height", d => chart.height - y(d.y))
            .attr("width", x.bandwidth())
            .style("fill", "steelblue")
            .on("mouseenter", (e, d) => {
                tooltip._groups[0][0].innerHTML = hoverfn(d);
                tooltip
                    .style("opacity", 1);
            })
            .on("mousemove", (e, d) =>
                tooltip
                    .style("top", (e.pageY - 180) + "px")
                    .style("left", (e.pageX) + "px")
            )
            .on("mouseleave", () =>
                tooltip
                    .style("opacity", 0)
            );
}

/**
 * Draws a radar/spider chart visualizing data.
 * @param {string} selector querySelector parameter.
 * @param {{ axis: string, value: number }[][]} data data to visualize.
 */
chart.radar = (selector, data) => {
    RadarChart(selector, data, {
        w: 400,
        h: 400,
        levels: 5,
        margin: {
            top: 80,
            right: 80,
            bottom: 80,
            left: 80
        }
    });
};
