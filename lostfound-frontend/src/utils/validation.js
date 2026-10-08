export function isValidEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

export function validateRegisterForm({ name, email, password }) {
  const errors = {};
  if (!name.trim()) errors.name = "Name is required.";
  if (!email.trim()) {
    errors.email = "Email is required.";
  } else if (!isValidEmail(email)) {
    errors.email = "Enter a valid email address.";
  }
  if (!password) {
    errors.password = "Password is required.";
  } else if (password.length < 8) {
    errors.password = "Password must be at least 8 characters.";
  }
  return errors;
}

export function validateItemForm({ title, categoryId, itemType, isCreate }) {
  const errors = {};
  if (!title.trim()) errors.title = "Title is required.";
  if (!categoryId) errors.categoryId = "Please select a category.";
  if (isCreate && !itemType) errors.itemType = "Please choose Lost or Found.";
  return errors;
}

export function validateClaimForm({ proofDescription }) {
  const errors = {};
  if (!proofDescription.trim()) {
    errors.proofDescription = "Please describe why this item belongs to you.";
  } else if (proofDescription.trim().length < 10) {
    errors.proofDescription = "Please add a bit more detail (at least 10 characters).";
  }
  return errors;
}

export function validateSubscriptionForm({ categoryId, keyword }) {
  const errors = {};
  if (!categoryId && !keyword.trim()) {
    errors.general = "Choose a category, enter a keyword, or both.";
  }
  return errors;
}

export function validateLoginForm({ email, password }) {
  const errors = {};
  if (!email.trim()) {
    errors.email = "Email is required.";
  } else if (!isValidEmail(email)) {
    errors.email = "Enter a valid email address.";
  }
  if (!password) errors.password = "Password is required.";
  return errors;
}
