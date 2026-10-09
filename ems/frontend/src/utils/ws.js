import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

export function createStompClient({ onConnect, onError } = {}) {
    const token = localStorage.getItem("token");

    const client = new Client({
        // token in query (SockJS can't reliably send Authorization headers on XHR)
        webSocketFactory: () => new SockJS(token ? `/ws?token=${encodeURIComponent(token)}` : "/ws"),
        connectHeaders: {}, // keep empty
        debug: () => {},
        reconnectDelay: 3000,
        onConnect,
        onStompError: onError,
    });

    return client;
}
