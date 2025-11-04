import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './RegisterPage.css';

function Register() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        username: '',
        password: '',
        name: '',
        address: '',
        age: '',
    });
    const [error, setError] = useState('');

    const API_URL = import.meta.env.VITE_API_URL;

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const res = await fetch(`${API_URL}/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    ...formData,
                    age: parseInt(formData.age),
                    role: 'CLIENT',
                }),
            });

            if (!res.ok) {
                const data = await res.text();
                throw new Error(data || 'Registration failed');
            }

            navigate('/login');
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="register-container">
            <form className="register-form" onSubmit={handleSubmit}>
                <h2>Register</h2>
                <input type="text" name="username" placeholder="Username" onChange={handleChange} required />
                <input type="password" name="password" placeholder="Password" onChange={handleChange} required />
                <input type="text" name="name" placeholder="Full Name" onChange={handleChange} required />
                <input type="text" name="address" placeholder="Address" onChange={handleChange} required />
                <input type="number" name="age" placeholder="Age" onChange={handleChange} required />
                <button type="submit">Register</button>
                {error && <p className="error">{error}</p>}
                <p>Already have an account? <Link to="/login">Login</Link></p>
            </form>
        </div>
    );
}

export default Register;
