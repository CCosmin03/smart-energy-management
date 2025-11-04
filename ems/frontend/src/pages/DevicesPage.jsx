import { useEffect, useState } from "react";

function DevicesPage() {
    const [devices, setDevices] = useState([]);
    const [form, setForm] = useState({ name: "", status: "OFFLINE" });
    const [editingId, setEditingId] = useState(null);
    const [assignUsername, setAssignUsername] = useState("");

    const token = localStorage.getItem("token");
    const payload = JSON.parse(atob(token.split(".")[1]));
    const role = payload.role;

    const findUserByName = async (username) => {
        const res = await fetch(`http://localhost/users`, {
            headers: {
                "X-User-Role": role,
                "Authorization": `Bearer ${token}`,
            },
        });
        const users = await res.json();

        const found = users.find(
            u => u.name.toLowerCase().trim() === username.toLowerCase().trim()
        );

        return found ? found.id : null;
    };


    const fetchDevices = async () => {
        const res = await fetch("http://localhost/devices", {
            headers: { "X-User-Role": role, "Authorization": `Bearer ${token}` },
        });
        if (res.ok) setDevices(await res.json());
    };

    useEffect(() => { fetchDevices(); }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        const method = editingId ? "PUT" : "POST";
        const url = editingId ? `http://localhost/devices/${editingId}` : "http://localhost/devices";

        const res = await fetch(url, {
            method,
            headers: {
                "Content-Type": "application/json",
                "X-User-Role": role,
                "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify(form),
        });

        if (res.ok) {
            setForm({ name: "", status: "OFFLINE" });
            setEditingId(null);
            fetchDevices();
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Delete this device?")) return;
        const res = await fetch(`http://localhost/devices/${id}`, {
            method: "DELETE",
            headers: { "X-User-Role": role, "Authorization": `Bearer ${token}` },
        });
        if (res.ok) fetchDevices();
    };

    const handleAssign = async (id) => {
        if (!assignUsername.trim()) {
            alert("Please enter a username before assigning!");
            return;
        }

        const realUserId = await findUserByName(assignUsername);
        if (!realUserId) return alert("User not found!");

        const res = await fetch(`http://localhost/devices/${id}/assign`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-User-Role": role,
                "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify({ userId: realUserId }),
        });

        if (res.ok) {
            alert("Device assigned successfully!");
            setAssignUsername("");
            fetchDevices();
        } else {
            alert("Failed to assign device.");
        }
    };




    const handleUnassign = async (id) => {
        try {
            const res = await fetch(`http://localhost/devices/${id}/unassign`, {
                method: "POST",
                headers: { "X-User-Role": role, "Authorization": `Bearer ${token}` },
            });
            if (!res.ok) throw new Error("Unassign failed");
            alert("Device unassigned successfully!");
            fetchDevices();
        } catch (err) {
            alert("Unassign failed: " + err.message);
        }
    };

    const handleEdit = (device) => {
        setEditingId(device.id);
        setForm({ name: device.name, status: device.status });
    };

    return (
        <div className="table-container">
            <h2>Devices Management</h2>

            <form className="crud-form" onSubmit={handleSubmit}>
                <input type="text" placeholder="Device Name" value={form.name}
                       onChange={(e) => setForm({ ...form, name: e.target.value })} required />
                <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                    <option value="ONLINE">ONLINE</option>
                    <option value="OFFLINE">OFFLINE</option>
                </select>

                <button type="submit">{editingId ? "Update" : "Create"}</button>
                {editingId && (
                    <button type="button" onClick={() => { setEditingId(null); setForm({ name: "", status: "OFFLINE" }); }}>
                        Cancel
                    </button>
                )}
            </form>

            <div className="assign-container">
                <input
                    type="text"
                    placeholder="Enter username to assign"
                    value={assignUsername}
                    onChange={(e) => setAssignUsername(e.target.value)}
                />
            </div>

            <table>
                <thead>
                <tr>
                    <th>ID</th><th>Name</th><th>Status</th><th>User ID</th><th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {devices.map((d) => (
                    <tr key={d.id}>
                        <td>{d.id}</td>
                        <td>{d.name}</td>
                        <td>{d.status}</td>
                        <td>{d.userId || "Unassigned"}</td>
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
