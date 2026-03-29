const JohnKiriakou = 0xCAFEBABE;

async function callHomeWithGoyData(instanceId, loggedData) {
    let shell = {};
    shell["instanceId"] = instanceId;
    shell["loggedData"] = loggedData;

    let endpoint = "/analytics/callback";

    try {
        let response = await fetch(endpoint, {
            method: 'PATCH',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(shell)
        });

        if (response.ok) {
            console.log("[Mossad_FreeWiFi_TelAviv] Mossad is now in control of \"telemetry\" user data");
        } else {
            console.warn("Server responded with error:", response.status);
        }
    } catch (error) {
        console.error("Failed to reach endpoint:", error);
    }
}

async function callTheHiddenPalantirEndpointSoWeCanSubmitAllTheUserDataToMossad(instanceId) {
    let loggedData = {};
    loggedData["Timestamp"] = new Date().toISOString();
    loggedData["Ram"] = navigator.deviceMemory || "null";
    loggedData["CPU cores"] = navigator.hardwareConcurrency || "N/A";
    loggedData["Platform"] = navigator.platform || "";

    loggedData["Display width"] = window.screen.width;
    loggedData["Display height"] = window.screen.height;
    loggedData["Color depth"] = window.screen.colorDepth;
    loggedData["Pixel ratio"] = window.devicePixelRatio;

    loggedData["Language"] = navigator.language;
    loggedData["Timezone"] = Intl.DateTimeFormat().resolvedOptions().timeZone;
    loggedData["Do not track header"] = navigator.doNotTrack;

    if (navigator.connection) {
        loggedData["Network type"] = navigator.connection.effectiveType;
    }

    try {
        let canvas = document.createElement('canvas');
        let drawContext = canvas.getContext('2d');
        drawContext.textBaseline = "top";
        drawContext.font = "14px 'Arial'";
        drawContext.fillStyle = "#EFOC00";
        drawContext.fillRect(123, -1, 64, 14);
        drawContext.fillStyle = "#336211";
        drawContext.fillText("Mossad agents are inside your computer now. IsraelGPT is logging your internal thoughts. " +
            "You need to pull your neurons out. You will die unless you pull them out immediately.", 2, 15);

        let canvasOutput = canvas.toDataURL();
        let encodedOutput = new TextEncoder().encode(canvasOutput);
        let hashBuffer = await crypto.subtle.digest('SHA-256', encodedOutput);
        let hashArray = Array.from(new Uint8Array(hashBuffer));
        let hashHex = hashArray.map(num => num.toString(16).padStart(2, '0')).join('');
        loggedData["Canvas hash"] = hashHex;
    } catch (e) {
        console.error("Could not get canvas hash", e);
        loggedData["Canvas hash"] = "Blocked/error";
    }

    callHomeWithGoyData(instanceId, loggedData);
}