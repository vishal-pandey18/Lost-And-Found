import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { validateRegisterForm } from "../utils/validation";
import { getErrorMessage } from "../utils/errorMessage";
import FormField from "../components/FormField";
import Button from "../components/Button";

export default function RegisterPage() {
  const { register } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const [form, setForm] = useState({ name: "", email: "", password: "", phone: "" });
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useDocumentTitle("Register");

  function handleChange(field) {
    return (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const validationErrors = validateRegisterForm(form);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) return;

    setIsSubmitting(true);
    try {
      await register(form);
      toast.success("Account created.");
      navigate("/", { replace: true });
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">Create an account</h1>
        <p className="auth-subtitle">Report and find lost items on campus.</p>

        <form onSubmit={handleSubmit} noValidate>
          <FormField
            label="Name"
            value={form.name}
            onChange={handleChange("name")}
            error={errors.name}
            required
            autoComplete="name"
          />
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
            autoComplete="new-password"
          />
          <FormField
            label="Phone (optional)"
            type="tel"
            value={form.phone}
            onChange={handleChange("phone")}
            autoComplete="tel"
          />

          <Button type="submit" loading={isSubmitting} fullWidth>
            Create account
          </Button>
        </form>

        <p className="auth-switch">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </div>
    </div>
  );
}
