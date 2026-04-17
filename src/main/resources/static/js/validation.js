document.addEventListener('DOMContentLoaded', function() {
    
    // Define allowed patterns to prevent special characters dynamically
    const patterns = {
        name: {
            regex: /^[a-zA-ZÀ-ÿ\s\'-]*$/,
            message: "Caractères spéciaux non autorisés. Utilisez uniquement des lettres."
        },
        alphanumeric: {
            regex: /^[a-zA-Z0-9\s-]*$/,
            message: "Caractères spéciaux non autorisés. Utilisez uniquement des lettres et des chiffres."
        },
        phone: {
            regex: /^[0-9\s+]*$/,
            message: "Seuls les chiffres et le signe + sont autorisés."
        },
        number: {
            regex: /^[0-9]*$/,
            message: "Ce champ ne doit contenir que des chiffres."
        }
    };

    // Configuration array matching the field IDs in our forms
    const fieldsToValidate = [
        { id: 'nom', rule: patterns.name },
        { id: 'prenom', rule: patterns.name },
        { id: 'nomComplet', rule: patterns.name },
        { id: 'mecanicien', rule: patterns.name },
        
        { id: 'marque', rule: patterns.alphanumeric },
        { id: 'modele', rule: patterns.alphanumeric },
        { id: 'plaqueImmatriculation', rule: patterns.alphanumeric },
        { id: 'typeEntretien', rule: patterns.alphanumeric },
        { id: 'reference', rule: patterns.alphanumeric },
        { id: 'numeroPermis', rule: patterns.alphanumeric },
        
        { id: 'telephone', rule: patterns.phone },
        { id: 'cin', rule: patterns.number }
    ];

    fieldsToValidate.forEach(config => {
        const input = document.getElementById(config.id);
        if (input) {
            
            // Find or create the feedback div
            let feedback = input.parentNode.querySelector('.invalid-feedback');
            if(!feedback) {
                feedback = document.createElement('div');
                feedback.className = 'invalid-feedback';
                // Find where to append it based on UI structure
                if(input.parentNode.classList.contains('input-group')) {
                    input.parentNode.parentNode.appendChild(feedback);
                } else {
                    input.parentNode.appendChild(feedback);
                }
            }

            // Real-time validation on typing
            input.addEventListener('input', function(e) {
                const val = input.value;
                if (!config.rule.regex.test(val)) {
                    // Invalid
                    input.classList.add('is-invalid');
                    feedback.textContent = config.rule.message;
                    feedback.style.display = 'block';
                } else {
                    // Valid
                    input.classList.remove('is-invalid');
                    // We let Spring Validation errors persist if they are there on load, 
                    // but we hide our direct typing error if it becomes valid.
                    feedback.textContent = '';
                    feedback.style.display = 'none';
                }
            });

            // Prevent form submission if the field is currently invalid (with our specific flag)
            const form = input.closest('form');
            if(form) {
                form.addEventListener('submit', function(e) {
                    if (input.classList.contains('is-invalid') && feedback.textContent === config.rule.message) {
                        e.preventDefault();
                        input.focus();
                    }
                });
            }
        }
    });

});
