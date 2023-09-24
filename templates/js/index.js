/**
 * Switch execution context on home page.
 * @param context Context ID.
 */
function switchCtx(context) {
    const contexts = [
        "corpus-index",
        "visualization",
        "full-text",
        "messaging"
    ].map(ctx => document.getElementById(ctx));
    for (const ctx of contexts) ctx.style.display = "none";
    document.getElementById(context).style.display = "flex";
}

/**
 * Send message to recipient over WebSocket.
 * @param message Message content.
 * @param recipient Recipient user.
 */
function sendMessage(message, recipient) {
    const sender = document.getElementById("userDropdown").querySelector("span").innerHTML.match(/<.*>(.*)/)[1];
    window.ws.send(JSON.stringify({
        sender, recipient, message
    }));
}
