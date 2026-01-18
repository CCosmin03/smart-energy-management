import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AdminDashboard from './pages/AdminDashboard';
import ClientDashboard from './pages/ClientDashboard.jsx';
import NotFound from "./pages/NotFound";
import AdminChatPage from './pages/AdminChatPage.jsx';
import ClientChatPage from './pages/ClientChatPage.jsx';

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Navigate to="/login" />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/admin" element={<AdminDashboard />} />
                <Route path="/client" element={<ClientDashboard />} />
                <Route path="/client/chat" element={<ClientChatPage />} />
                <Route path="/admin/chat" element={<AdminChatPage />} />
                <Route path="*" element={<NotFound />} />
            </Routes>
        </Router>
    );
}

export default App;
