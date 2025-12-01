import { useEffect, useState } from "react";
import "./ClientDashboard.css";
import {
    LineChart,
    Line,
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ResponsiveContainer,
} from "recharts";

function ClientDashboard() {
    const [devices, setDevices] = useState([]);
    const [loadingDevices, setLoadingDevices] = useState(true);
    const [devicesError, setDevicesError] = useState("");

    const [selectedDeviceId, setSelectedDeviceId] = useState("");
    const [selectedDate, setSelectedDate] = useState(
        new Date().toISOString().split("T")[0] // azi, format YYYY-MM-DD
    );

    const [consumption, setConsumption] = useState([]); // listă de 24 valori
    const [loadingConsumption, setLoadingConsumption] = useState(false);
    const [consumptionError, setConsumptionError] = useState("");

    const API_URL = import.meta.env.VITE_API_URL;

    // ===================== 1. Fetch devices =====================
    useEffect(() => {
        const fetchDevices = async () => {
            try {
                const token = localStorage.getItem("token");
                if (!token) {
                    window.location.href = "/login";
                    return;
                }

                const payload = JSON.parse(atob(token.split(".")[1]));
                const userId = payload.userId;
                const role = payload.role;

                const res = await fetch(`${API_URL}/devices/user/${userId}`, {
                    headers: {
                        "Authorization": `Bearer ${token}`,
                        "X-User-Id": userId,
                        "X-User-Role": role,
                    },
                });

                if (!res.ok) throw new Error("Failed to load devices");
                const data = await res.json();
                setDevices(data);

                // dacă are cel puțin un device, îl selectăm automat
                if (data.length > 0) {
                    setSelectedDeviceId(data[0].id);
                }
            } catch (err) {
                console.error(err);
                setDevicesError("Could not load your devices.");
            } finally {
                setLoadingDevices(false);
            }
        };

        fetchDevices();
    }, [API_URL]);

    // ===================== 2. Fetch consumption =====================
    useEffect(() => {
        const fetchConsumption = async () => {
            if (!selectedDeviceId || !selectedDate) return;

            try {
                setLoadingConsumption(true);
                setConsumptionError("");

                const token = localStorage.getItem("token");
                if (!token) {
                    window.location.href = "/login";
                    return;
                }

                const res = await fetch(
                    `${API_URL}/monitoring/device/${selectedDeviceId}?date=${selectedDate}`,
                    {
                        headers: {
                            "Authorization": `Bearer ${token}`,
                        },
                    }
                );

                if (!res.ok) {
                    throw new Error("Failed to load consumption data");
                }

                // Backend întoarce List<Double> de 24 elemente
                const data = await res.json();
                setConsumption(data);
            } catch (err) {
                console.error(err);
                setConsumptionError("Could not load consumption data for this day.");
                setConsumption([]);
            } finally {
                setLoadingConsumption(false);
            }
        };

        fetchConsumption();
    }, [API_URL, selectedDeviceId, selectedDate]);

    // ===================== 3. Transform data pentru Recharts =====================
    const chartData = consumption.map((value, hour) => ({
        hour, // 0..23
        consumption: value ?? 0,
    }));

    const handleLogout = () => {
        localStorage.clear();
        window.location.href = "/login";
    };

    return (
        <div className="client-dashboard">
            <header className="header">
                <h1>Your Energy Dashboard</h1>
                <button onClick={handleLogout} className="logout-btn">
                    Logout
                </button>
            </header>

            <main className="content">
                {/* === Secțiune device-uri === */}
                {loadingDevices ? (
                    <p>Loading your devices...</p>
                ) : devicesError ? (
                    <p className="error">{devicesError}</p>
                ) : devices.length === 0 ? (
                    <p>You have no assigned devices.</p>
                ) : (
                    <>
                        <section className="device-section">
                            <h2>Your Devices</h2>
                            <table>
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Name</th>
                                    <th>Status</th>
                                    <th>Max Consumption (W)</th>
                                </tr>
                                </thead>
                                <tbody>
                                {devices.map((device) => (
                                    <tr
                                        key={device.id}
                                        className={
                                            device.id === selectedDeviceId
                                                ? "selected-row"
                                                : ""
                                        }
                                        onClick={() => setSelectedDeviceId(device.id)}
                                    >
                                        <td>{device.id}</td>
                                        <td>{device.name}</td>
                                        <td>{device.status}</td>
                                        <td>{device.maxConsumption}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </section>

                        {/* === Secțiune grafic consum === */}
                        <section className="chart-section">
                            <div className="chart-controls">
                                <div>
                                    <label>Device:&nbsp;</label>
                                    <select
                                        value={selectedDeviceId}
                                        onChange={(e) =>
                                            setSelectedDeviceId(e.target.value)
                                        }
                                    >
                                        {devices.map((d) => (
                                            <option key={d.id} value={d.id}>
                                                {d.name}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                <div>
                                    <label>Date:&nbsp;</label>
                                    <input
                                        type="date"
                                        value={selectedDate}
                                        onChange={(e) => setSelectedDate(e.target.value)}
                                    />
                                </div>
                            </div>

                            {loadingConsumption ? (
                                <p>Loading consumption...</p>
                            ) : consumptionError ? (
                                <p className="error">{consumptionError}</p>
                            ) : chartData.length === 0 ? (
                                <p>No data for this device/day.</p>
                            ) : (
                                <div className="charts-wrapper">
                                    <div className="chart-card">
                                        <h3>Hourly Consumption (Line)</h3>
                                        <ResponsiveContainer width="100%" height={300}>
                                            <LineChart data={chartData}>
                                                <CartesianGrid strokeDasharray="3 3" />
                                                <XAxis
                                                    dataKey="hour"
                                                    label={{
                                                        value: "Hour",
                                                        position: "insideBottomRight",
                                                        offset: -5,
                                                    }}
                                                />
                                                <YAxis
                                                    label={{
                                                        value: "kWh",
                                                        angle: -90,
                                                        position: "insideLeft",
                                                    }}
                                                />
                                                <Tooltip />
                                                <Legend />
                                                <Line
                                                    type="monotone"
                                                    dataKey="consumption"
                                                    dot={false}
                                                />
                                            </LineChart>
                                        </ResponsiveContainer>
                                    </div>

                                    <div className="chart-card">
                                        <h3>Hourly Consumption (Bar)</h3>
                                        <ResponsiveContainer width="100%" height={300}>
                                            <BarChart data={chartData}>
                                                <CartesianGrid strokeDasharray="3 3" />
                                                <XAxis dataKey="hour" />
                                                <YAxis />
                                                <Tooltip />
                                                <Legend />
                                                <Bar dataKey="consumption" />
                                            </BarChart>
                                        </ResponsiveContainer>
                                    </div>
                                </div>
                            )}
                        </section>
                    </>
                )}
            </main>
        </div>
    );
}

export default ClientDashboard;
