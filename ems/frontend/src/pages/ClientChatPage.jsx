import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { createStompClient } from "../utils/ws";
import "./ClientChatPage.css";

function decodeJwtPayload(token) {
    try {
        return JSON.parse(atob(token.split(".")[1]));
    } catch {
        return null;
    }
}

export default function ClientChatPage() {
    const navigate = useNavigate();

    const token = useMemo(() => localStorage.getItem("token"), []);
    const payload = useMemo(() => (token ? decodeJwtPayload(token) : null), [token]);

    const myUserId = payload?.userId || "";
    const myRole = payload?.role || "";

    // tabs: AI vs ADMIN
    const [mode, setMode] = useState("AI"); // "AI" | "ADMIN"

    // toggle rule-based for AI (persist)
    const [useRules, setUseRules] = useState(() => {
        const v = localStorage.getItem("chat_useRules");
        return v === "true";
    });

    const [input, setInput] = useState("");

    // Separate histories:
    const [messagesAI, setMessagesAI] = useState([]);
    const [messagesAdmin, setMessagesAdmin] = useState([]);

    const stompRef = useRef(null);
    const bottomRef = useRef(null);

    useEffect(() => {
        if (!token || !myUserId) navigate("/login");
        if (myRole === "ADMIN") navigate("/admin/chat");
    }, [token, myUserId, myRole, navigate]);

    useEffect(() => {
        localStorage.setItem("chat_useRules", String(useRules));
    }, [useRules]);

    // WS connect + subscribe
    useEffect(() => {
        if (!token || !myUserId) return;

        const stomp = createStompClient({
            onConnect: () => {
                stomp.subscribe(`/topic/user.${myUserId}`, (frame) => {
                    try {
                        const msg = JSON.parse(frame.body);

                        // We route by msg.channel if exists, else fallback:
                        // - if receiverId === "ADMIN" or senderType === "ADMIN" => ADMIN history
                        // - else => AI history
                        const ch =
                            (msg.channel && String(msg.channel).toUpperCase()) ||
                            (msg.senderType === "ADMIN" || msg.receiverId === "ADMIN" ? "ADMIN" : "AI");

                        if (ch === "ADMIN") {
                            setMessagesAdmin((prev) => [...prev, msg]);
                        } else {
                            setMessagesAI((prev) => [...prev, msg]);
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
    }, [token, myUserId]);

    // scroll to bottom when current tab changes or messages update
    useEffect(() => {
        bottomRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [mode, messagesAI, messagesAdmin]);

    const handleBack = () => {
        navigate("/client");
    };

    const sendClientMessageToSupport = async ({ message, useRulesOverride }) => {
        const res = await fetch(`http://localhost/api/chat`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
                userId: myUserId,
                message,
                useRules: useRulesOverride, // Boolean
            }),
        });

        if (!res.ok) throw new Error("Failed to send message");
    };

    const onSend = async () => {
        const text = input.trim();
        if (!text) return;

        setInput("");

        try {
            if (mode === "ADMIN") {
                // Force admin routing using prefix (backend strict checks)
                await sendClientMessageToSupport({
                    message: `[ADMIN] ${text}`,
                    useRulesOverride: false,
                });
                // No optimistic UI needed — backend echoes to WS
                return;
            }

            // AI mode
            await sendClientMessageToSupport({
                message: text,
                useRulesOverride: useRules,
            });
            // No optimistic UI — backend echoes to WS
        } catch (e) {
            console.error(e);
            alert("Could not send message.");
        }
    };

    const activeMessages = mode === "ADMIN" ? messagesAdmin : messagesAI;

    return (
        <div className="client-chat-page">
            <header className="client-chat-header">
                <div className="client-chat-title">
                    <h1>Customer Support</h1>
                    <p className="muted">
                        {mode === "AI"
                            ? "Chat with AI (optional quick rules)."
                            : "Request / talk to an administrator."}
                    </p>
                </div>

                <div className="client-chat-actions">
                    <div className="mode-tabs">
                        <button
                            className={mode === "AI" ? "active" : ""}
                            onClick={() => setMode("AI")}
                        >
                            AI
                        </button>
                        <button
                            className={mode === "ADMIN" ? "active" : ""}
                            onClick={() => setMode("ADMIN")}
                        >
                            Admin
                        </button>
                    </div>

                    {mode === "AI" && (
                        <label className="rules-toggle">
                            <input
                                type="checkbox"
                                checked={useRules}
                                onChange={(e) => setUseRules(e.target.checked)}
                            />
                            Use quick rules
                        </label>
                    )}

                    <button className="btn-secondary" onClick={handleBack}>
                        Back
                    </button>
                </div>
            </header>

            <main className="client-chat-main">
                <div className="messages">
                    {activeMessages.map((m, idx) => {
                        const mine = m.senderType === "CLIENT" && m.senderId === myUserId;
                        return (
                            <div
                                key={`${m.timestamp || idx}-${idx}`}
                                className={`msg ${mine ? "mine" : "theirs"}`}
                            >
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
                        placeholder={mode === "AI" ? "Ask AI..." : "Write to admin..."}
                        onKeyDown={(e) => {
                            if (e.key === "Enter") onSend();
                        }}
                    />
                    <button onClick={onSend}>Send</button>
                </div>
            </main>
        </div>
    );
}
