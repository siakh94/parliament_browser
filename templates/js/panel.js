window.c = 0;

const funcMap = elem => ({ x: elem.specifier, y: elem.count });

/**
 * Generates a panel base to append charts.
 * @param {number} c ID counter.
 * @returns {HTMLDivElement} Panel base div.
 */
function panelBase(c, title) {
    const box = document.createElement("div");
    const container = document.createElement("div");
    container.id = "panel-container-" + c;
    box.innerHTML = `
        <div class="d-flex">
            <form
                class="d-none d-sm-inline-block form-inline mr-auto ml-md-3 my-2 my-md-0 mw-100 navbar-search">
                <div class="input-group">
                    <input type="text" class="form-control bg-light border-0 small" placeholder="${title}" disabled=1
                        aria-label="Search" aria-describedby="basic-addon2">
                    <div class="input-group-append">
                        <button class="btn btn-primary" type="button" disabled=1>
                            <i class="fas fa-search fa-sm"></i>
                        </button>
                    </div>
                </div>
            </form>
            <button
                onclick="(() => { const el = document.getElementById('${container.id}'); el.style.display === 'none' ? el.style.display = 'block' : el.style.display = 'none'; })()"
                style="margin: 5px;"
                type="button"
                class="btn btn-outline-primary">
                Minimieren
            </button>
            <button
                onclick="this.parentNode.parentNode.remove()"
                style="margin: 5px;"
                type="button"
                class="btn btn-outline-primary">
                Schließen
            </button>
        </div>
    `;
    box.style = `
        padding: 30px;
        box.shadow: 10px 10px;
        background-color: white;
    `;
    box.appendChild(container);
    for (const id of ["tok-chart", "pos-chart", "sentiment-chart", "people-chart", "places-chart", "organizations-chart", "speakers-chart"]) {
        const child = document.createElement("div");
        child.id = id + "-" + c;
        container.appendChild(child);
    }
    return box;
}

/**
 * Spawns a search panel.
 * @param {string} query Text query filter.
 * @param {number} from UNIX timestamp (GMT+1, CET).
 * @param {number} until UNIX timestamp (GMT+1, CET).
 */
function spawnPanel(query, from, until) {
    const parameters = {};
    if (query) parameters.query = query;
    if (from) parameters.from = from;
    if (until) parameters.until = until;
    fetch("/search?" + new URLSearchParams(parameters).toString(), {
        method: "GET"
    }).then(res => {
        if (res.status !== 200) throw new Error("invalid response while spawning panel");
        return res.json();
    }).then(data => {
        const base = panelBase(window.c, query);
        document.getElementById("search-panels").appendChild(base);
        const { TOK: tok, POS: pos, sentiment, NE: ne, speakers } = data;
        tok.sort((a, b) => b.count - a.count);
        pos.sort((a, b) => b.count - a.count);
        ne.people.sort((a, b) => b.count - a.count);
        ne.places.sort((a, b) => b.count - a.count);
        ne.organizations.sort((a, b) => b.count - a.count);
        speakers.sort((a, b) => b.count - a.count);
        chart.line(`#tok-chart-${window.c}`, tok.slice(0, 70).map(funcMap));
        chart.bar(`#pos-chart-${window.c}`, pos.slice(0, 70).map(funcMap), d => `
            ${d.x}<br>
            ${d.y}
        `);
        chart.radar(`#sentiment-chart-${window.c}`, [[
            { axis: "Negativ", value: sentiment.negative },
            { axis: "Neutral", value: sentiment.neutral },
            { axis: "Positiv", value: sentiment.positive }
        ]]);
        chart.line(`#people-chart-${window.c}`, ne.people.slice(0, 70).map(funcMap));
        chart.line(`#places-chart-${window.c}`, ne.places.slice(0, 70).map(funcMap));
        chart.line(`#organizations-chart-${window.c}`, ne.organizations.slice(0, 70).map(funcMap));
        console.log(speakers);
        chart.bar(`#speakers-chart-${window.c}`, speakers.slice(0, 70).map(speaker => ({
            x: speaker.name,
            y: speaker.count,
            ...speaker
        })), speakerPanel);
        window.c++;
    });
}

/**
 * Spawns a panel for testing purposes.
 */
function testPanel() {
    const test_numeric = (n) => {
        let fn;
        if (Math.random() > 0.667)
            fn = Math.sin;
        else if (Math.random() > 0.5)
            fn = Math.cos;
        else
            fn = Math.tan;
        return Array.from({ length: n }, (_, i) => ({ x: i, y: Math.abs(fn(Math.PI/8*i)) }));
    };
    
    const test_radar = [
        [{axis: "t0", value: Math.random() * 100}, {axis: "t1", value: Math.random() * 100}, {axis: "t2", value: Math.random() * 100}],
        [{axis: "t0", value: Math.random() * 100}, {axis: "t1", value: Math.random() * 100}, {axis: "t2", value: Math.random() * 100}],
        [{axis: "t0", value: Math.random() * 100}, {axis: "t1", value: Math.random() * 100}, {axis: "t2", value: Math.random() * 100}]
    ]

    const base = panelBase(window.c, "test query");
    document.getElementById("search-panels").appendChild(base);
    chart.line(`#tok-chart-${window.c}`, test_numeric(250), true);
    chart.bar(`#pos-chart-${window.c}`, test_numeric(10), (d) => speakerPanel({
        name: "Example",
        id: "example_id",
        image: "https://study.com/cimages/multimages/16/solid_shape_dice.jpg",
        fraction: {
            name: "Example Fraction",
            id: "example_id2"
        },
        count: d.y
    }));
    chart.radar(`#sentiment-chart-${window.c}`, test_radar);
    chart.line(`#people-chart-${window.c}`, test_numeric(250), true);
    chart.line(`#places-chart-${window.c}`, test_numeric(250), true);
    chart.line(`#organizations-chart-${window.c}`, test_numeric(250), true);
    chart.bar(`#speakers-chart-${window.c}`, test_numeric(250), (d) => speakerPanel({
        name: "Example",
        id: "example_id",
        image: "https://study.com/cimages/multimages/16/solid_shape_dice.jpg",
        fraction: {
            name: "Example Fraction",
            id: "example_id2"
        },
        count: d.y
    }));
    window.c++;
}

/**
 * Neatly displays a speaker's information in a panel.
 * @param {{ name: string, id: string, image: string, fraction?: { name: string, id: string }, party?: { name: string, id: string }, count: number }} data Speaker data.
 */
function speakerPanel(data) {
    const { name, id, image, fraction, party, count } = data ?? { name: "Unbekannter Redner", id: "", image: "https://study.com/cimages/multimages/16/solid_shape_dice.jpg", fraction: null, count: 0 };
    const fractionStr = fraction ? `
        <div style="margin: 10px;">
            Partei: ${party.name} (${party.id})<br>
            Fraktion: ${fraction.name} (${fraction.id})
        </div>
    ` : "";
    const countStr = count ? `
        <div class="d-flex flex-column"><div style="margin: 10px;">
            Reden: ${count}
        </div>
    ` : "";
    return `
        <div class="d-flex">
            <img width="50" height="50" style="margin: 10px;" src="${image}">
            <div class="d-flex flex-column"><div style="margin: 10px;">
                ${name} (${id})
            </div>
            ${fractionStr}
            ${countStr}
        </div>
    `;
}
