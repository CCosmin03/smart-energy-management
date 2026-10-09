import { useEffect, useState } from "react";

function DevicesPage() {
    const [devices, setDevices] = useState([]);
    const [assignments, setAssignments] = useState([]);
    const [form, setForm] = useState({ name: "", status: "OFFLINE", maxConsumption: 0 });
    const [editingId, setEditingId] = useState(null);
    const [assignUserId, setAssignUserId] = useState("");

    const token = localStorage.getItem("token");
    const payload = JSON.parse(atob(token.split(".")[1]));
    const role = payload.role;

    //const API = "http://localhost";

    // === Fetch all devices ===
    const fetchDevices = async () => {
        const res = await fetch(`http://localhost/api/devices`, {
            headers: { "X-User-Role": role, "Authorization": `Bearer ${token}` },
        });
        if (res.ok) {
            setDevices(await res.json());
        }
    };

    // === Fetch all assignments ===
    const fetchAssignments = async () => {
        const res = await fetch(`http://localhost/api/assignments`, {
            headers: { "Authorization": `Bearer ${token}` },
        });
        if (res.ok) {
            setAssignments(await res.json());
        }
    };

    useEffect(() => {
        fetchDevices();
        fetchAssignments();
    }, []);

    // === CREATE / UPDATE DEVICE ===
    const handleSubmit = async (e) => {
        e.preventDefault();
        const method = editingId ? "PUT" : "POST";
        const url = editingId
            ? `http://localhost/api/devices/${editingId}`
            : `http://localhost/api/devices`;

        const payload = {
            name: form.name,
            status: form.status,
            maxConsumption: Number(form.maxConsumption)
        };

        const res = await fetch(url, {
            method,
            headers: {
                "Content-Type": "application/json",
                "X-User-Role": role,
                "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify(payload),
        });

        if (res.ok) {
            setForm({ name: "", status: "OFFLINE", maxConsumption: 0 });
            setEditingId(null);
            fetchDevices();
        }
    };

    // === DELETE DEVICE ===
    const handleDelete = async (id) => {
        if (!window.confirm("Delete this device?")) return;
        const res = await fetch(`http://localhost/api/devices/${id}`, {
            method: "DELETE",
            headers: { "X-User-Role": role, "Authorization": `Bearer ${token}` },
        });
        if (res.ok) {
            fetchDevices();
            fetchAssignments();
        }
    };

    // === ASSIGN (BY userId) ===
    const handleAssign = async (deviceId) => {
        if (!assignUserId.trim()) {
            alert("Please enter a userId!");
            return;
        }

        const res = await fetch(`http://localhost/api/assignments`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify({
                userId: assignUserId,
                deviceId: deviceId,
            }),
        });

        if (res.ok) {
            alert("Device assigned!");
            setAssignUserId("");
            fetchAssignments();
        } else {
            alert("Failed to assign device.");
        }
    };

    // === UNASSIGN ===
    const handleUnassign = async (deviceId) => {
        const res = await fetch(`http://localhost/api/assignments/${deviceId}`, {
            method: "DELETE",
            headers: { "Authorization": `Bearer ${token}` },
        });

        if (res.ok) {
            alert("Device unassigned!");
            fetchAssignments();
        } else {
            alert("Failed to unassign.");
        }
    };

    // === HELPER: get userId for a device ===
    const getUserForDevice = (deviceId) => {
        const a = assignments.find(a => a.deviceId === deviceId);
        return a ? a.userId : "Unassigned";
    };

    const handleEdit = (device) => {
        setEditingId(device.id);
        setForm({ name: device.name, status: device.status, maxConsumption: device.maxConsumption ?? 0 });
    };

    return (
        <div className="table-container">
            <h2>Devices Management</h2>

            {/* CRUD FORM */}
            <form className="crud-form" onSubmit={handleSubmit}>
                <input
                    type="text"
                    placeholder="Device Name"
                    value={form.name}
                    onChange={(e) => setForm({ ...form, name: e.target.value })}
                    required
                />

                <select
                    value={form.status}
                    onChange={(e) => setForm({ ...form, status: e.target.value })}
                >
                    <option value="ONLINE">ONLINE</option>
                    <option value="OFFLINE">OFFLINE</option>
                </select>

                <input
                    type="number"
                    placeholder="Max Consumption (W)"
                    value={form.maxConsumption}
                    onChange={(e) => setForm({ ...form, maxConsumption: e.target.value })}
                />

                <button type="submit">{editingId ? "Update" : "Create"}</button>

                {editingId && (
                    <button
                        type="button"
                        onClick={() => {
                            setEditingId(null);
                            setForm({ name: "", status: "OFFLINE", maxConsumption: 0 });
                        }}
                    >
                        Cancel
                    </button>
                )}
            </form>

            {/* ASSIGN INPUT */}
            <div className="assign-container">
                <input
                    type="text"
                    placeholder="Enter userId to assign"
                    value={assignUserId}
                    onChange={(e) => setAssignUserId(e.target.value)}
                />
            </div>

            {/* DEVICES TABLE */}
            <table>
                <thead>
                <tr>
                    <th>ID</th><th>Name</th><th>Status</th><th>Max Consumption (W)</th><th>User ID</th><th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {devices.map((d) => (
                    <tr key={d.id}>
                        <td>{d.id}</td>
                        <td>{d.name}</td>
                        <td>{d.status}</td>
                        <td>{d.maxConsumption}</td>
                        <td>{getUserForDevice(d.id)}</td>
                        <td>
                            <button onClick={() => handleEdit(d)}>Edit</button>
                            <button className="danger" onClick={() => handleDelete(d.id)}>Delete</button>
                            <button onClick={() => handleAssign(d.id)}>Assign</button>
                            <button onClick={() => handleUnassign(d.id)}>Unassign</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default DevicesPage;
