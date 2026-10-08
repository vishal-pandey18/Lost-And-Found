import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { validateLoginForm } from "../utils/validation";
import { getErrorMessage } from "../utils/errorMessage";
import FormField from "../components/FormField";
import Button from "../components/Button";

export default function LoginPage() {
  const { login } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const [form, setForm] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const redirectTo = location.state?.from?.pathname || "/";

  useDocumentTitle("Log in");

  function handleChange(field) {
    return (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const validationErrors = validateLoginForm(form);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) return;

    setIsSubmitting(true);
    try {
      await login(form);
      toast.success("Welcome back.");
      navigate(redirectTo, { replace: true });
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">Log in</h1>
        <p className="auth-subtitle">Welcome back to Lost &amp; Found.</p>

        <form onSubmit={handleSubmit} noValidate>
          <FormField
            label="Email"
            type="email"
            value={form.email}
            onChange={handleChange("email")}
            error={errors.email}
            required
            autoComplete="email"
          />
          <FormField
            label="Password"
            type="password"
            value={form.password}
            onChange={handleChange("password")}
            error={errors.password}
            required
            autoComplete="current-password"
          />

          <Button type="submit" loading={isSubmitting} fullWidth>
            Log in
          </Button>
        </form>

        <p className="auth-switch">
          Don&apos;t have an account? <Link to="/register">Register</Link>
        </p>
      </div>
    </div>
  );
}
