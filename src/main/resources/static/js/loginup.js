document.addEventListener('DOMContentLoaded', function() {
        let currentStep = 1;
        const totalSteps = 3;
        const progressFill = document.getElementById('progress-fill');
        const nextButton = document.getElementById('next-button');
        const backButton = document.getElementById('back-button');
        const submitButton = document.getElementById('submit-button');
        const emailField = document.getElementById('email');
        const passwordField = document.getElementById('password');
        const hiddenEmail = document.getElementById('hidden-email');
        const hiddenPassword = document.getElementById('hidden-password');
        const errorMessage = document.getElementById('error-message');

        // Si hay un error del servidor, ir directamente al paso 3
        const serverError = document.querySelector('[th\\:if="${error}"]');
        if (serverError && serverError.style.display !== 'none') {
            currentStep = 3;
            // Llenar los campos ocultos con los valores existentes
            if (emailField.value) {
                hiddenEmail.value = emailField.value;
            }
        }

        // Actualizar progreso
        function updateProgress() {
            const progress = ((currentStep - 1) / (totalSteps - 1)) * 100;
            progressFill.style.width = `${progress}%`;

            // Actualizar estados de los pasos
            document.querySelectorAll('.step-circle').forEach((circle, index) => {
                circle.classList.remove('active', 'completed');
                if (index + 1 < currentStep) {
                    circle.classList.add('completed');
                } else if (index + 1 === currentStep) {
                    circle.classList.add('active');
                }
            });

            // Mostrar/ocultar contenidos
            document.querySelectorAll('[id$="-content"]').forEach((content, index) => {
                content.style.display = index + 1 === currentStep ? 'block' : 'none';
            });

            // Mostrar/ocultar botones
            nextButton.style.display = currentStep < totalSteps ? 'block' : 'none';
            backButton.style.display = currentStep > 1 ? 'block' : 'none';
            submitButton.style.display = currentStep === totalSteps ? 'block' : 'none';

            // Actualizar texto del botón siguiente
            if (currentStep === totalSteps - 1) {
                nextButton.textContent = 'Finalizar';
            } else {
                nextButton.textContent = 'Siguiente';
            }
        }

        // Validar email
        function validateEmail(email) {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            return emailRegex.test(email);
        }

        // Validar contraseña
        function validatePassword(password) {
            const hasMinLength = password.length >= 8;
            const hasUpperCase = /[A-Z]/.test(password);
            const hasNumber = /\d/.test(password);

            // Actualizar indicadores visuales
            document.getElementById('hint-length').className = `password-hint ${hasMinLength ? 'hint-valid' : 'hint-invalid'}`;
            document.getElementById('hint-uppercase').className = `password-hint ${hasUpperCase ? 'hint-valid' : 'hint-invalid'}`;
            document.getElementById('hint-number').className = `password-hint ${hasNumber ? 'hint-valid' : 'hint-invalid'}`;

            // Actualizar fuerza de contraseña
            const strengthFill = document.querySelector('.password-strength-fill');
            const strengthContainer = document.querySelector('.password-strength');

            strengthContainer.classList.remove('password-weak', 'password-medium', 'password-strong');

            if (hasMinLength && hasUpperCase && hasNumber) {
                strengthContainer.classList.add('password-strong');
            } else if ((hasMinLength && hasUpperCase) || (hasMinLength && hasNumber)) {
                strengthContainer.classList.add('password-medium');
            } else if (hasMinLength) {
                strengthContainer.classList.add('password-weak');
            }

            return hasMinLength && hasUpperCase && hasNumber;
        }

        // Manejar clic en siguiente - ACTUALIZADO con mensajes más específicos
        nextButton.addEventListener('click', function() {
            let isValid = true;

            if (currentStep === 1) {
                if (!validateEmail(emailField.value)) {
                    errorMessage.textContent = 'Por favor, introduce un email válido (Gmail, Hotmail, Outlook, etc.).';
                    errorMessage.style.display = 'block';
                    isValid = false;
                } else {
                    hiddenEmail.value = emailField.value;
                    errorMessage.style.display = 'none';
                }
            } else if (currentStep === 2) {
                if (!validatePassword(passwordField.value)) {
                    errorMessage.textContent = 'La contraseña no cumple con los requisitos de seguridad.';
                    errorMessage.style.display = 'block';
                    isValid = false;
                } else {
                    hiddenPassword.value = passwordField.value;
                    errorMessage.style.display = 'none';
                }
            }

            if (isValid && currentStep < totalSteps) {
                currentStep++;
                updateProgress();
            }
        });

        // Manejar clic en atrás
        backButton.addEventListener('click', function() {
            if (currentStep > 1) {
                currentStep--;
                updateProgress();
                errorMessage.style.display = 'none';
            }
        });

        // Validar contraseña en tiempo real
        passwordField.addEventListener('input', function() {
            validatePassword(passwordField.value);
        });

        // NUEVO: Validar email en tiempo real para dar feedback inmediato
        emailField.addEventListener('blur', function() {
            if (emailField.value && !validateEmail(emailField.value)) {
                emailField.style.borderColor = '#ef4444';
            } else {
                emailField.style.borderColor = '#e2e8f0';
            }
        });

        // Inicializar
        updateProgress();
    });