function getFieldValues() {
  const email = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value.trim();
  return { email, password };
}

function isEmailEmpty(email) {
  return email === '';
}

function isPasswordEmpty(password) {
  return password === '';
}

function showMessage(text, type) {
  const statusEl = document.getElementById('status-message');
  statusEl.textContent = text;
  statusEl.className = 'status-message ' + type;
}

function validateLoginForm() {
  const { email, password } = getFieldValues();
  const missingFields = [];

  if (isEmailEmpty(email)) missingFields.push('Email');
  if (isPasswordEmpty(password)) missingFields.push('Password');

  if (missingFields.length > 0) {
    showMessage(`Please fill in the following field(s): ${missingFields.join(', ')}`, 'error');
    return false;
  }
  return true;
}

async function handleLoginClick() {
  const isValid = validateLoginForm();
  if (!isValid) return;

  const { email, password } = getFieldValues();
  showMessage('Logging in...', 'success');

  const result = await AuthAPI.login(email, password);

  if (result.success) {
    showMessage(result.message, 'success');

    if (result.token) {
      localStorage.setItem('token', result.token);
    }

    setTimeout(() => {
      window.location.href = 'dashboard.html';
    }, 600);
  } else {
    showMessage(result.message, 'error');
  }
}

function init() {
  const loginButton = document.querySelector('.login-button');
  loginButton.addEventListener('click', handleLoginClick);
}

document.addEventListener('DOMContentLoaded', init);