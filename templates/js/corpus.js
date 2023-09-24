/**
 * Minimizable div container with an "open" button.
 * @returns {[HTMLDivElement, HTMLDivElement, HTMLDivElement]} Round box div, container div, "open" button.
 */
function minimizeBox(title) {
    const gbox = document.createElement("div");
    gbox.classList.add("card", "border-left-primary", "shadow", "m-2");
    gbox.innerHTML = `
        <h3 class="m-2">${title}</h3>
        <div>
            <button
                style="margin: 5px;"
                type="button"
                class="btn btn-outline-primary m-2">
                Öffnen
            </button>
        </div>
    `;
    const gcontainer = document.createElement("div");
    gcontainer.classList.add("card", "shadow", "h-100", "py-2", "m-2");
    gcontainer.style.display = "none";
    gbox.appendChild(gcontainer);
    /** @type {HTMLButtonElement} */
    const minimize = gbox.childNodes[3].childNodes[1];
    minimize.onclick = function() {
        const el = this.parentNode.parentNode.childNodes[5];
        if (el.style.display === "none") {
            this.innerHTML = "Minimieren";
            el.style.display = "block";
        } else {
            this.innerHTML = "Öffnen";
            el.style.display = "none";
        }
    }
    return [gcontainer, gbox, gbox.childNodes[3].childNodes[1]];
}

/**
 * Turns response into object.
 * @param {Response} res Response object of request.
 * @returns 
 */
function tojson(res) {
    if (res.status >= 300) throw new Error("invalid response while spawning panel");
    return res.json();
}

/**
 * Performs a corpus search and displays relevant entries.
 * @param {string} query Text query filter.
 * @param {number} from UNIX timestamp (GMT+1, CET).
 * @param {number} until UNIX timestamp (GMT+1, CET).
 * @param {"day" | "week" | "month" | "year" | "none"} groupBy Data grouping. Defaults to "none".
 */
async function spawnCorpusSearch(query, from, until, groupBy = "none") {
    const parameters = {};
    if (query) parameters.query = query;
    if (from) parameters.from = from;
    if (until) parameters.until = until;
    parameters.groupBy = groupBy;
    const { groups } = await fetch("/corpus?" + new URLSearchParams(parameters).toString(), {
        method: "GET"
    }).then(tojson);
    const results = document.getElementById("corpus-results");
    results.innerHTML = "";
    for (const group of groups) {
        const { groupName, protocols } = group;
        const [groupContainer, groupBox] = minimizeBox(groupName);
        results.appendChild(groupBox);
        for (const protocol of protocols) {
            const { id: protocolId, title: protocolTitle } = protocol;
            const [protocolContainer, protocolBox, protocolButton] = minimizeBox(protocolTitle);
            protocolButton.setAttribute("fetch-url", "/protocol?id=" + encodeURIComponent(protocolId));
            const editPButton = document.createElement("button");
            editPButton.classList.add("btn", "btn-outline-primary", "m-2");
            editPButton.type = "button";
            editPButton.innerHTML = "Bearbeiten";
            editPButton.onclick = () => window.location = "/edit/protocol?id=" + encodeURIComponent(protocolId);
            const toPdfB = document.createElement("button");
            toPdfB.classList.add("btn", "btn-outline-primary", "m-2");
            toPdfB.type = "button";
            toPdfB.innerHTML = "PDF Export"
            toPdfB.onclick = () => window.location = "/templated/protocol?id=" + encodeURIComponent(protocolId);
            protocolBox.appendChild(editPButton);
            protocolBox.appendChild(toPdfB);
            groupContainer.appendChild(protocolBox);
            const protocolPrev = protocolButton.onclick;
            protocolButton.onclick = async function() {
                const { agendaItems } = protocol.agendaItems ? protocol : await fetch(this.getAttribute("fetch-url"), {
                    method: "GET"
                }).then(tojson);
                for (const agendaItem of agendaItems) {
                    const { id: agendaId, title: agendaTitle } = agendaItem;
                    const [agendaContainer, agendaBox, agendaButton] = minimizeBox(agendaTitle);
                    agendaButton.setAttribute("fetch-url", "/agendaItem?id=" + encodeURIComponent(agendaId));
                    const editAButton = document.createElement("button");
                    editAButton.classList.add("btn", "btn-outline-primary", "m-2");
                    editAButton.type = "button";
                    editAButton.innerHTML = "Bearbeiten";
                    editAButton.onclick = () => window.location = "/edit/agendaItem?id=" + encodeURIComponent(agendaId);
                    agendaBox.appendChild(editAButton);
                    protocolContainer.appendChild(agendaBox);
                    const agendaPrev = agendaButton.onclick;
                    agendaButton.onclick = async function() {
                        const { speeches } = agendaItem.speeches ? agendaItem : await fetch(this.getAttribute("fetch-url"), {
                            method: "GET"
                        }).then(tojson);
                        for (const speech of speeches) {
                            const { id: speechId, speaker } = speech;
                            const speakerPan = speakerPanel(speaker);
                            const b = document.createElement("div");
                            b.setAttribute("fetch-url", "/speech?id=" + encodeURIComponent(speechId));
                            b.innerHTML = speakerPan;
                            agendaContainer.appendChild(b);
                            const editSButton = document.createElement("button");
                            editSButton.classList.add("btn", "btn-outline-primary", "m-2");
                            editSButton.type = "button";
                            editSButton.innerHTML = "Bearbeiten";
                            editSButton.onclick = () => window.location = "/edit/speech?id=" + encodeURIComponent(speechId);
                            const prevSButton = document.createElement("button");
                            prevSButton.classList.add("btn", "btn-outline-primary", "m-2");
                            prevSButton.type = "button";
                            prevSButton.innerHTML = "Volltext Ansicht";
                            prevSButton.onclick = async function() {
                                document.getElementById("full-text").innerHTML = "";
                                switchCtx("full-text");
                                const data = await fetch(this.parentNode.getAttribute("fetch-url"), {
                                    method: "GET"
                                }).then(tojson);
                                const { id: speechIdNested, speaker: speakerNested } = data;
                                /** @type {{ sentiment: number, text: string, namedEntities: { type: string, position: { begin: number, end: number } }[] }[]} */
                                const sentences = data.sentences;
                                const speechbox = document.createElement("div");
                                speechbox.classList.add("card", "border-left-primary", "shadow", "h-100", "py-2", "m-2");
                                speechbox.innerHTML = `
                                    <div style="color: black;">
                                        <h3>Legende</h3>
                                        Grün: <span style="background-color: green;">PER</span><br>
                                        Rot: <span style="background-color: red;">LOC</span><br>
                                        Dunkelblau: <span style="background-color: #6756d6;">ORG</span><br>
                                        Magenta: <span style="background-color: magenta;">MISC</span>
                                    </div>
                                ` + speakerPanel(speakerNested);
                                const textbox = document.createElement("div");
                                textbox.style.color = "black";
                                speechbox.appendChild(textbox);
                                for (const { sentiment, text, namedEntities } of sentences) {
                                    if (text.replaceAll(" ", "").length === 0) continue;
                                    const outDiv = document.createElement("div");
                                    outDiv.classList.add("d-inline-block", "mx-1");
                                    const sentimentElement = document.createElement("button");
                                    sentimentElement.type = "button";
                                    sentimentElement.classList.add("btn", "btn-sm", "btn-warning", "d-inline-block");
                                    sentimentElement.innerHTML = sentiment.toPrecision(3);
                                    let lastEnd = 0;
                                    for (const { type, position } of namedEntities) {
                                        const neEl = document.createElement("span");
                                        neEl.style.backgroundColor = type === "PER" ? "green"   :
                                                                     type === "LOC" ? "red"     :
                                                                     type === "ORG" ? "#6756d6" :
                                                                     "magenta";
                                        neEl.innerHTML = text.slice(position.begin, position.end);
                                        outDiv.innerHTML += text.slice(lastEnd, position.begin) + neEl.outerHTML;
                                        lastEnd = position.end;
                                    }
                                    outDiv.innerHTML += text.slice(lastEnd);
                                    outDiv.appendChild(sentimentElement);
                                    textbox.appendChild(outDiv);
                                }
                                document.getElementById("full-text").appendChild(speechbox);
                            }
                            b.appendChild(prevSButton);
                            b.appendChild(editSButton);
                        }
                        this.onclick = agendaPrev;
                        this.onclick();
                    }
                }
                this.onclick = protocolPrev;
                this.onclick();
            }
        }
    }
}
