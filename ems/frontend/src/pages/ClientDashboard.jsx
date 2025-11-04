import { useEffect, useState } from "react";
import "./ClientDashboard.css";

function ClientDashboard() {
    const [devices, setDevices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const API_URL = import.meta.env.VITE_API_URL;

    useEffect(() => {
        const fetchDevices = async () => {
            try {
                const token = localStorage.getItem("token");
                const payload = JSON.parse(atob(token.split(".")[1]));
                console.log("Token payload:", payload);

                const userId = payload.userId;
                const role = payload.role;

                const res = await fetch(`${API_URL}/devices/user/${userId}`, {
                    headers: {
                        "Authorization": `Bearer ${token}`,
                        "X-User-Id": userId,
                        "X-User-Role": role,
                    },
                });

                if (!res.ok) throw new Error("Failed to fetch assigned devices");
                const data = await res.json();
                setDevices(data);
            } catch (err) {
                console.error(err);
                setError("Could not load your devices.");
            } finally {
                setLoading(false);
            }
        };

        fetchDevices();
    }, []);

    const handleLogout = () => {
        localStorage.clear();
        window.location.href = "/login";
    };

    return (
        <div className="client-dashboard">
            <header className="header">
                <h1>Your Devices</h1>
                <button onClick={handleLogout} className="logout-btn">Logout</button>
            </header>
            <main className="content">
                {loading ? (
                    <p>Loading devices...</p>
                ) : error ? (
                    <p className="error">{error}</p>
                ) : devices.length === 0 ? (
                    <p>You have no assigned devices.</p>
                ) : (
                    <table>
                        <thead>
                        <tr><th>ID</th><th>Name</th><th>Status</th></tr>
                        </thead>
                        <tbody>
                        {devices.map((device) => (
                            <tr key={device.id}>
                                <td>{device.id}</td>
                                <td>{device.name}</td>
                                <td>{device.status}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </main>
        </div>
    );
}

export default ClientDashboard;
