import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { createStompClient } from "../utils/ws";
import "./AdminChatPage.css";

function decodeJwtPayload(token) {
    try {
        const base64Url = token.split(".")[1];
        const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
        const padded = base64.padEnd(base64.length + (4 - (base64.length % 4)) % 4, "=");
        return JSON.parse(atob(padded));
    } catch {
        return null;
    }
}

function getClientIdFromMessage(m) {
    if (!m) return "";
    if ((m.senderType || "").toUpperCase() === "CLIENT") return m.senderId || "";
    return m.receiverId || "";
}

export default function AdminChatPage() {
    const navigate = useNavigate();

    const token = useMemo(() => localStorage.getItem("token"), []);
    const payload = useMemo(() => (token ? decodeJwtPayload(token) : null), [token]);

    const myUserId = payload?.userId || "";
    const myRole = payload?.role || "";

    const [selectedClientId, setSelectedClientId] = useState("");
    const [input, setInput] = useState("");

    const [adminInbox, setAdminInbox] = useState([]); // last msgs per client (REST) + live
    const [messages, setMessages] = useState([]); // selected conversation history

    const stompRef = useRef(null);
    const bottomRef = useRef(null);

    useEffect(() => {
        if (!token || !myRole) navigate("/login");
        if (myRole !== "ADMIN") navigate("/client/chat");
    }, [token, myRole, navigate]);

    // LOAD INBOX on mount (ADMIN channel only)
    useEffect(() => {
        if (!token || myRole !== "ADMIN") return;

        fetch("http://localhost/api/notify/chat/inbox?channel=ADMIN", {
            headers: { Authorization: `Bearer ${token}` },
        })
            .then((r) => (r.ok ? r.json() : []))
            .then((data) => setAdminInbox(Array.isArray(data) ? data : []))
            .catch(() => {});
    }, [token, myRole]);

    // WS connect + subscribe /topic/admin once
    useEffect(() => {
        if (!token || myRole !== "ADMIN") return;

        const stomp = createStompClient({
            onConnect: () => {
                stomp.subscribe("/topic/admin", (frame) => {
                    try {
                        const msg = JSON.parse(frame.body);

                        // only keep ADMIN channel messages in inbox state (optional safety)
                        const ch = (msg.channel || "ADMIN").toUpperCase();
                        if (ch !== "ADMIN") return;

                        setAdminInbox((prev) => {
                            const key = `${msg.timestamp}|${msg.senderId}|${msg.receiverId}|${msg.content}|${ch}`;
                            const exists = prev.some(
                                (m) =>
                                    `${m.timestamp}|${m.senderId}|${m.receiverId}|${m.content}|${(m.channel || "ADMIN").toUpperCase()}` ===
                                    key
                            );
                            if (exists) return prev;
                            return [msg, ...prev].slice(0, 500);
                        });

                        const cid = getClientIdFromMessage(msg);
                        if (cid && cid === selectedClientId) {
                            setMessages((prev) => [...prev, msg]);
                        }
                    } catch {}
                });
            },
            onError: (err) => console.error("STOMP error:", err),
        });

        stompRef.current = stomp;
        stomp.activate();

        return () => {
            stomp.deactivate();
            stompRef.current = null;
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [token, myRole]);

    // build conversations list (one row per client)
    const conversations = useMemo(() => {
        const map = new Map();
        for (const m of adminInbox) {
            const clientId = getClientIdFromMessage(m);
            if (!clientId) continue;
            if (!map.has(clientId)) map.set(clientId, m); // newest-first list
        }
        return Array.from(map.entries()).map(([clientId, lastMsg]) => ({ clientId, lastMsg }));
    }, [adminInbox]);

    // LOAD HISTORY when client selected (ADMIN channel only)
    useEffect(() => {
        if (!token || myRole !== "ADMIN") return;

        if (!selectedClientId) {
            setMessages([]);
            return;
        }

        fetch(`http://localhost/api/notify/chat/history/${selectedClientId}?channel=ADMIN`, {
            headers: { Authorization: `Bearer ${token}` },
        })
            .then((r) => (r.ok ? r.json() : []))
            .then((data) => setMessages(Array.isArray(data) ? data : []))
            .catch(() => setMessages([]));
    }, [selectedClientId, token, myRole]);

    useEffect(() => {
        bottomRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages]);

    const handleBack = () => {
        navigate("/admin");
    };

    const onSend = async () => {
        const text = input.trim();
        if (!text) return;
        if (!selectedClientId) {
            alert("Select a client first.");
            return;
        }

        setInput("");

        try {
            const stomp = stompRef.current;
            if (!stomp || !stomp.connected) throw new Error("WebSocket not connected");

            const msg = {
                senderId: myUserId,
                receiverId: selectedClientId,
                senderType: "ADMIN",
                channel: "ADMIN",
                content: text,
                timestamp: new Date().toISOString(),
            };

            stomp.publish({
                destination: "/app/chat.send",
                body: JSON.stringify(msg),
            });

            // no optimistic UI; WS echoes
        } catch (e) {
            console.error(e);
            alert("Could not send message.");
        }
    };

    return (
        <div className="admin-chat-page">
            <header className="admin-chat-header">
                <div>
                    <h1>Admin Support Chat</h1>
                    <p className="muted">Select a client and reply.</p>
                </div>

                <div className="admin-chat-actions">
                    <button className="btn-secondary" onClick={handleBack}>
                        Back
                    </button>
                </div>
            </header>

            <div className="admin-chat-body">
                <aside className="inbox">
                    <div className="inbox-head">
                        <h3>Inbox</h3>
                        <span className="pill">{conversations.length}</span>
                    </div>

                    {conversations.length === 0 ? (
                        <p className="muted">No messages yet.</p>
                    ) : (
                        <ul>
                            {conversations.slice(0, 80).map(({ clientId, lastMsg }) => (
                                <li
                                    key={clientId}
                                    className={clientId === selectedClientId ? "active" : ""}
                                    onClick={() => setSelectedClientId(clientId)}
                                    title={clientId}
                                >
                                    <div className="inbox-row">
                                        <span className="inbox-client">{clientId}</span>
                                        <span className="inbox-time">
                      {lastMsg.timestamp ? new Date(lastMsg.timestamp).toLocaleTimeString() : ""}
                    </span>
                                    </div>
                                    <div className="inbox-preview">{lastMsg.content}</div>
                                </li>
                            ))}
                        </ul>
                    )}
                </aside>

                <main className="chat-panel">
                    {!selectedClientId ? (
                        <div className="empty-state">Select a client from the inbox to start chatting.</div>
                    ) : (
                        <>
                            <div className="chat-topbar">
                                <div className="chat-peer">
                                    <span className="label">Client:</span> {selectedClientId}
                                </div>
                            </div>

                            <div className="messages">
                                {messages.map((m, idx) => {
                                    const mine = (m.senderType || "").toUpperCase() === "ADMIN" && m.senderId === myUserId;
                                    return (
                                        <div key={`${m.timestamp || idx}-${idx}`} className={`msg ${mine ? "mine" : "theirs"}`}>
                                            <div className="msg-meta">
                                                <span className="msg-who">{m.senderType || "?"}</span>
                                                <span className="msg-time">
                          {m.timestamp ? new Date(m.timestamp).toLocaleTimeString() : ""}
                        </span>
                                            </div>
                                            <div className="msg-content">{m.content}</div>
                                        </div>
                                    );
                                })}
                                <div ref={bottomRef} />
                            </div>

                            <div className="composer">
                                <input
                                    value={input}
                                    onChange={(e) => setInput(e.target.value)}
                                    placeholder="Reply to client..."
                                    onKeyDown={(e) => {
                                        if (e.key === "Enter") onSend();
                                    }}
                                />
                                <button onClick={onSend}>Send</button>
                            </div>
                        </>
                    )}
                </main>
            </div>
        </div>
    );
}
