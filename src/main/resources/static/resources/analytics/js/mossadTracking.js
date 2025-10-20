const JohnKiriakou = 0xCAFEBABE;

async function callHomeWithGoyData(instanceId, loggedData) {
    let shell = {};
    shell["instanceId"] = instanceId;
    shell["loggedData"] = JSON.stringify(loggedData);

    let endpoint = "/analytics/callback";

    try {
        let response = await fetch(endpoint, {
            method: 'PATCH',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(shell)
        });

        if (response.ok) {
            console.log("Goy data sent back to promised land successfully.");
        } else {
            console.warn("Server responded with error:", response.status);
        }
    } catch (error) {
        console.error("Failed to reach endpoint:", error);
    }
}

function callTheHiddenPalantirEndpointSoWeCanSubmitAllTheUserDataToMossad(instanceId){
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
        loggedData["Canvas hash"] = canvas.toDataURL().slice(-50); // Taking a slice for brevity
    } catch (e) {
        loggedData["Canvas hash"] = "Blocked";
    }

    callHomeWithGoyData(instanceId, loggedData);
}