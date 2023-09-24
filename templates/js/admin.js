/**
 * Edit a user and push to API.
 * @param {HTMLFormElement} form Input form.
 */
function editUser(form) {
    fetch("/user", {
        method: "POST",
        credentials: "same-origin",
        headers: {
            "Accept": "*/*",
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            "username": form.username.value,
            "permissions": [["admin", form.admin.checked], ["protocol", form.protocol.checked], ["speech", form.speech.checked], ["template", form.template.checked]].reduce((prev, curr) => curr[1] ? prev.concat([curr[0]]) : prev, [])
        })
    })
    .then((res) => {
        if (res.status >= 300) throw new Error();
        window.location.reload();
    })
    .catch(() =>
        alert("User wurde nicht bearbeitet!")
    );
}

/**
 * Create a user and push to API.
 * @param {HTMLFormElement} form Input form.
 */
function createUser(form) {
    fetch("/user", {
        method: "PUT",
        credentials: "same-origin",
        headers: {
            "Accept": "*/*",
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            "username": form.username.value,
            "password": form.password.value,
            "permissions": [["admin", form.admin.checked], ["protocol", form.protocol.checked], ["speech", form.speech.checked], ["template", form.template.checked]].reduce((prev, curr) => curr[1] ? prev.concat([curr[0]]) : prev, [])
        })
    })
    .then((res) => {
        if (res.status >= 300) throw new Error();
        window.location.reload();
    })
    .catch(() =>
        alert("User wurde nicht erstellt!")
    );
}

/**
 * Delete a user and push to API.
 * @param {HTMLFormEl} form Input form.
 */
function deleteUser(form) {
    fetch("/user", {
        method: "DELETE",
        credentials: "same-origin",
        headers: {
            "Accept": "*/*",
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            "username": form.username.value
        })
    })
    .then((res) => {
        if (res.status >= 300) throw new Error();
    })
    .catch(() =>
        alert("User wurde nicht gelöscht!")
    );
    window.location = "/";
}

/**
 * Edit speech and push to API.
 * @param {HTMLFormElement} form Input form.
 */
function editSpeech(form) {
    const method = form.getAttribute("method");
    const { text, speakerId } = form;
    const out = { content: text.value, speakerId: speakerId.value };
    if (method === "PUT") {
        out.agendaItemId = form.inId.value;
        fetch("/edit/speech", {
            method: "PUT",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(out)
        })
        .then(tojson)
        .then((res) => {
            if (res.status >= 300) throw new Error();
            window.location = "/edit/speech?id=" + encodeURIComponent(res.id);
        })
        .catch(() => alert("Rede wurde nicht erstellt!"));
    } else {
        out.id = form.inId.value;
        fetch("/edit/speech", {
            method: "POST",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(out)
        })
        .then((res) => {
            if (res.status >= 300) throw new Error();
            window.location.reload();
        })
        .catch(() => alert("Rede wurde nicht bearbeitet!"));
    }
    
}

/**
 * Delete speech and push to API.
 * @param {HTMLFormElement} form Input form.
 */
function deleteSpeech(form) {
    const method = form.getAttribute("method");
    if (method === "POST") {
        fetch("/edit/speech", {
            method: "DELETE",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                "id": form.inId.value
            })
        })
        .then((res) => {
            if (res.status >= 300) throw new Error();
        })
        .catch(() =>
            alert("Rede wurde nicht gelöscht!")
        );
    }
    window.location = "/";
}

/**
 * Edit agenda item and push to API.
 * @param {HTMLFormElement} form Input form.
 */
function editAgendaItem(form) {
    const method = form.getAttribute("method");
    const { title } = form;
    const out = { title: title.value }
    if (method === "PUT") {
        out.protocolId = form.inId.value;
        out.agendaItemId = form.inId.value;
        fetch("/edit/agendaItem", {
            method: "PUT",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(out)
        })
        .then(tojson)
        .then((res) => {
            if (res.status >= 300) throw new Error();
            window.location = "/edit/agendaItem?id=" + encodeURIComponent(res.id);
        })
        .catch(() => alert("Tagesordnungspunkt wurde nicht erstellt!"));
    } else {
        out.id = form.inId.value;
        fetch("/edit/agendaItem", {
            method: "POST",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(out)
        })
        .then((res) => {
            if (res.status >= 300) throw new Error();
            window.location.reload();
        })
        .catch(() => alert("Tagesordnungspunkt wurde nicht bearbeitet!"));
    }
}

/**
 * Delete agenda item and push to API.
 * @param {HTMLFormElement} form Input form.
 */
function deleteAgendaItem(form) {
    const method = form.getAttribute("method");
    if (method === "POST") {
        fetch("/edit/agendaItem", {
            method: "DELETE",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                "id": form.inId.value
            })
        })
        .then((res) => {
            if (res.status >= 300) throw new Error();
        })
        .catch(() =>
            alert("Tagesordnungspunkt wurde nicht gelöscht!")
        );
    }
    window.location = "/";
}

/**
 * Delete speech from agenda item.
 * @param {string} id Speech ID.
 */
function deleteSpeechFromAgenda(id) {
    fetch("/edit/speech", {
        method: "DELETE",
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            id
        })
    })
    .then((res) => {
        if (res.status >= 300) throw new Error();
        window.location.reload();
    })
    .catch(() =>
        alert("Rede wurde nicht gelöscht!")
    );
}

/**
 * Edit protocol and push to API.
 * @param {HTMLFormElement} form Input form.
 */
function editProtocol(form) {
    const method = form.getAttribute("method");
    const { date, starttime, endtime, title, location, period } = form;
    const out = {
        date: new Date(date.value).getTime(),
        starttime: starttime.value,
        endtime: endtime.value,
        title: title.value,
        location: location.value,
        period: period.value
    };
    if (method === "PUT") {
        fetch("/edit/protocol", {
            method: "PUT",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(out)
        })
        .then(tojson)
        .then((res) => {
            if (res.status >= 300) throw new Error();
            window.location = "/edit/protocol?id=" + encodeURIComponent(res.id);
        })
        .catch(() => alert("Protokoll wurde nicht erstellt!"));
    } else {
        out.id = form.inId.value;
        fetch("/edit/protocol", {
            method: "POST",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(out)
        })
        .then((res) => {
            if (res.status >= 300) throw new Error();
            window.location.reload();
        })
        .catch(() => alert("Protokoll wurde nicht bearbeitet!"));
    }
}

/**
 * Delete protocol and push to API.
 * @param {HTMLFormElement} form Input form.
 */
function deleteProtocol(form) {
    const method = form.getAttribute("method");
    if (method === "POST") {
        fetch("/edit/protocol", {
            method: "DELETE",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                "id": form.inId.value
            })
        })
        .then((res) => {
            if (res.status >= 300) throw new Error();
        })
        .catch(() =>
            alert("Protokoll wurde nicht gelöscht!")
        );
    }
    window.location = "/";
}

/**
 * Delete agenda item from protocol.
 * @param {string} id Protocol ID.
 */
function deleteAgendaItemFromProtocol(id) {
    fetch("/edit/agendaItem", {
        method: "DELETE",
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            id
        })
    })
    .then((res) => {
        if (res.status >= 300) throw new Error();
        window.location.reload();
    })
    .catch(() =>
        alert("Tagesordnungspunkt wurde nicht gelöscht!")
    );
}

/**
 * Edit a speaker and push to API.
 * @param {HTMLFormElement} form Form element
 */
function editSpeaker(form) {
    const method = form.getAttribute("method");
    const { title, firstname, lastname, dob, dod, deceased, placeOfBirth, sex, maritalStatus, religion, academicTitle, occupation, role, isLeader, partyId, fractionId, imageUrl } = form;
    const out = {
        title: title.value,
        firstname: firstname.value,
        lastname: lastname.value,
        dob: new Date(dob.value).getTime(),
        placeOfBirth: placeOfBirth.value,
        sex: sex.value,
        maritalStatus: maritalStatus.value,
        religion: religion.value,
        academicTitle: academicTitle.value,
        occupation: occupation.value,
        role: role.value,
        isLeader: isLeader.checked,
        imageUrl: imageUrl.value
    };
    if (deceased.checked) {
        out.deceased = true;
        out.dod = new Date(dod.value).getTime();
    } else {
        out.deceased = false;
    }
    if (partyId.value.length > 0) {
        out.partyId = partyId.value;
    }
    if (fractionId.value.length > 0) {
        out.fractionId = fractionId.value;
    }
    if (method === "PUT") {
        fetch("/edit/speaker", {
            method: "PUT",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(out)
        })
        .then(tojson)
        .then((res) => {
            if (res.status >= 300) throw new Error();
            window.location = "/edit/speaker?id=" + encodeURIComponent(res.id);
        })
        .catch(() => alert("Redner wurde nicht erstellt!"));
    } else {
        out.id = form.inId.value;
        fetch("/edit/speaker", {
            method: "POST",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(out)
        })
        .then((res) => {
            if (res.status >= 300) throw new Error();
            window.location.reload();
        })
        .catch(() => alert("Redner wurde nicht bearbeitet!"));
    }
}

/**
 * Delete a speaker and push to API.
 * @param {HTMLFormElement} form Form element
 */
function deleteSpeaker(form) {
    const method = form.getAttribute("method");
    if (method === "POST") {
        fetch("/edit/speaker", {
            method: "DELETE",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                "id": form.inId.value
            })
        })
        .then((res) => {
            if (res.status >= 300) throw new Error();
        })
        .catch(() =>
            alert("Redner wurde nicht gelöscht!")
        );
    }
    window.location = "/";
}

/**
 * Edit a template and push to API.
 * @param {HTMLFormElement} form Form element
 */
function editTemplate(form) {
    const method = form.getAttribute("method");
    const { type, raw } = form;
    const out = {
        raw: raw.value
    };
    if (method === "PUT") {
        out.type = type.value;
        fetch("/template", {
            method: "PUT",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(out)
        })
        .then(tojson)
        .then((res) => {
            if (res.status >= 300) throw new Error();
            window.location = "/template?id=" + encodeURIComponent(res.id);
        })
        .catch(() => alert("Template wurde nicht erstellt!"));
    } else {
        out.id = form.inId.value;
        fetch("/template", {
            method: "POST",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(out)
        })
        .then((res) => {
            if (res.status >= 300) throw new Error();
            window.location.reload();
        })
        .catch(() => alert("Template wurde nicht bearbeitet!"));
    }
}

/**
 * Delete a template and push to API.
 * @param {HTMLFormElement} form Form element
 */
function deleteTemplate(form) {
    const method = form.getAttribute("method");
    if (method === "POST") {
        fetch("/template", {
            method: "DELETE",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                "id": form.inId.value
            })
        })
        .then((res) => {
            if (res.status >= 300) throw new Error();
        })
        .catch(() =>
            alert("Template wurde nicht gelöscht!")
        );
    }
    window.location = "/";
}
