/**
 * Submit login form.
 */
function submitLogin() {
    /** @type {HTMLInputElement} */
    const username = document.getElementById("usernameInput");
    /** @type {HTMLInputElement} */
    const password = document.getElementById("passwordInput");
    fetch("/login", {
        method: "POST",
        credentials: "same-origin",
        headers: {
            "Accept": "*/*",
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            "username": username.value,
            "password": password.value
        })
    })
    .then((res) => {
        if (res.status >= 300) throw new Error();
        window.location.pathname = "/";
        window.location.reload();
    })
    .catch(() => 
        alert("Username oder Passwort inkorrekt!")
    );
}

/**
 * Logout (invalidate session cookie).
 */
function submitLogout() {
    fetch("/logout", {
        method: "GET",
        credentials: "same-origin"
    })
    .then((res) => {
        if (res.status >= 300) throw new Error();
        window.location.pathname = "/";
        window.location.reload();
    })
    .catch(() => 
        alert("Logout fehlgeschlagen!")
    );
}
