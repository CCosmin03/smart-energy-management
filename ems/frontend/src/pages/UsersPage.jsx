import { useEffect, useState } from "react";

function UsersPage() {
    const [users, setUsers] = useState([]);
    const [form, setForm] = useState({ name: "", address: "", age: "" });
    const [editingId, setEditingId] = useState(null);

    const token = localStorage.getItem("token");
    const payload = JSON.parse(atob(token.split(".")[1]));
    const role = payload.role;

    const fetchUsers = async () => {
        const res = await fetch("http://localhost/users", {
            headers: { "X-User-Role": role, "Authorization": `Bearer ${token}` },
        });
        if (res.ok) {
            const data = await res.json();

            const enriched = await Promise.all(
                data.map(async (u) => {
                    const detailRes = await fetch(`http://localhost/users/${u.id}`, {
                        headers: {
                            "X-User-Id": payload.userId || payload.id,
                            "X-User-Role": role,
                            "Authorization": `Bearer ${token}`,
                        },
                    });
                    if (detailRes.ok) {
                        const details = await detailRes.json();
                        return { ...u, address: details.address, age: details.age };
                    }
                    return u;
                })
            );
            setUsers(enriched);
        }
    };

    useEffect(() => { fetchUsers(); }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();

        const method = editingId ? "PUT" : "POST";
        const url = editingId ? `http://localhost/users/${editingId}` : "http://localhost/users";

        const res = await fetch(url, {
            method,
            headers: {
                "Content-Type": "application/json",
                "X-User-Id": payload.userId || payload.id,
                "X-User-Role": role,
                "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify({ ...form, age: parseInt(form.age) }),
        });

        if (res.ok) {
            setForm({ name: "", address: "", age: "" });
            setEditingId(null);
            fetchUsers();
        }
    };

    const handleEdit = (user) => {
        setEditingId(user.id);
        setForm({ name: user.name, address: user.address, age: user.age });
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Delete this user?")) return;
        const res = await fetch(`http://localhost/users/${id}`, {
            method: "DELETE",
            headers: { "X-User-Role": role, "Authorization": `Bearer ${token}` },
        });
        if (res.ok) fetchUsers();
    };

    return (
        <div className="table-container">
            <h2>Users Management</h2>

            <form className="crud-form" onSubmit={handleSubmit}>
                <input type="text" placeholder="Name" value={form.name}
                       onChange={(e) => setForm({ ...form, name: e.target.value })} required />
                <input type="text" placeholder="Address" value={form.address}
                       onChange={(e) => setForm({ ...form, address: e.target.value })} required />
                <input type="number" placeholder="Age" value={form.age}
                       onChange={(e) => setForm({ ...form, age: e.target.value })} required />

                <button type="submit">{editingId ? "Update" : "Create"}</button>
                {editingId && (
                    <button type="button" onClick={() => {
                        setEditingId(null);
                        setForm({ name: "", address: "", age: "" });
                    }}>
                        Cancel
                    </button>
                )}
            </form>

            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Address</th>
                    <th>Age</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {users.map((user) => (
                    <tr key={user.id}>
                        <td>{user.id}</td>
                        <td>{user.name}</td>
                        <td>{user.address || "—"}</td>
                        <td>{user.age || "—"}</td>
                        <td>
                            <button onClick={() => handleEdit(user)}>Edit</button>
                            <button className="danger" onClick={() => handleDelete(user.id)}>Delete</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default UsersPage;
