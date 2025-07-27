const onlyActiveCheckbox = document.getElementById('only-active-checkbox');
const exportToXmlBtn = document.getElementById('export-to-xml-btn');
const inactiveAppointmentsRows = document.querySelectorAll('.inactive-appointment');
const noActiveAppointmentsAlert = document.getElementById('no-active-appointments-alert');

if (document.querySelectorAll('.active-appointment').length === 0) {
    noActiveAppointmentsAlert.classList.remove('d-none');
}

onlyActiveCheckbox.addEventListener('change',  (event) => {

    const checked = event.target.checked;
    if (checked) {
        inactiveAppointmentsRows.forEach(row => {
            row.classList.add('d-none');
        });
        exportToXmlBtn.href = '/owner-dashboard/appointments/export-to-xml?active=true';
    } else {
        inactiveAppointmentsRows.forEach(row => {
            row.classList.remove('d-none');
        });
        exportToXmlBtn.href = '/owner-dashboard/appointments/export-to-xml?active=false';
    }
});


document.addEventListener('DOMContentLoaded', function () {
    const links = document.querySelectorAll('.customer-link');

    links.forEach(link => {
        link.addEventListener('click', function () {
            document.getElementById('modal-first-name').textContent = this.dataset.firstname;
            document.getElementById('modal-last-name').textContent = this.dataset.lastname;
            document.getElementById('modal-email').textContent = this.dataset.email;
            document.getElementById('modal-phone').textContent = this.dataset.phone;
        });
    });
});