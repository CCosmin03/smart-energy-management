import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./LoginPage.css";

function LoginPage() {
    const navigate = useNavigate();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    const API_URL = import.meta.env.VITE_API_URL;

    const handleLogin = async (e) => {
        e.preventDefault();
        setError("");

        try {
            const response = await fetch(`${API_URL}/auth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password }),
            });

            if (!response.ok) throw new Error("Login failed");
            const data = await response.json();

            const token = data.token;
            const payload = JSON.parse(atob(token.split(".")[1]));
            const role = payload.role;

            localStorage.setItem("token", token);
            localStorage.setItem("role", role);

            if (role === "ADMIN") navigate("/admin");
            else navigate("/client");
        } catch {
            setError("Invalid username or password.");
        }
    };

    return (
        <div className="login-container">
            <form className="login-card" onSubmit={handleLogin}>
                <h2>Welcome! Login here:</h2>

                <input
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />

                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />

                {error && <p className="error">{error}</p>}

                <button type="submit">Login</button>

                <p className="bottom-text">
                    Don't have an account?{" "}
                    <a href="/register" className="link">Register</a>
                </p>
            </form>
        </div>
    );
}

export default LoginPage;
