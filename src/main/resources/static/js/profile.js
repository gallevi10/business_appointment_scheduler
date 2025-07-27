const submitChangesBtn = document.getElementById('submit-changes-btn');
const editDetailsBtn = document.getElementById('edit-details-btn');
const formInputs = document.querySelectorAll('.profile-form-inputs');

editDetailsBtn.addEventListener('click', (event) => {

    formInputs.forEach(input => {
        input.disabled = false;
    });
    submitChangesBtn.classList.remove('d-none');
    editDetailsBtn.classList.add('d-none');
});