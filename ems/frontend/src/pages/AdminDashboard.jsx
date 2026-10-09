import { useState } from "react";
import UsersPage from "./UsersPage";
import DevicesPage from "./DevicesPage";
import "./AdminDashboard.css";

function AdminDashboard() {
    const [activePage, setActivePage] = useState("users");

    const handleLogout = () => {
        localStorage.clear();
        window.location.href = "/login";
    };

    return (
        <div className="admin-dashboard">
            <aside className="sidebar">
                <h2>Admin Dashboard</h2>
                <nav>
                    <button
                        className={activePage === "users" ? "active" : ""}
                        onClick={() => setActivePage("users")}
                    >
                        Users
                    </button>
                    <button
                        className={activePage === "chat" ? "active" : ""}
                        onClick={() => window.location.href = "/admin/chat"}
                    >
                        Chat
                    </button>

                    <button
                        className={activePage === "devices" ? "active" : ""}
                        onClick={() => setActivePage("devices")}
                    >
                        Devices
                    </button>
                </nav>

                <button className="logout-btn" onClick={handleLogout}>
                    Logout
                </button>
            </aside>

            <main className="content">
                {activePage === "users" && <UsersPage />}
                {activePage === "devices" && <DevicesPage />}
            </main>
        </div>
    );
}

export default AdminDashboard;
