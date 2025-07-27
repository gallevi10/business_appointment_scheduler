const dateInput = document.getElementById("date-input");
const timeSelect = document.getElementById("time-select");

const today = new Date();
const oneMonthLater = new Date();
oneMonthLater.setMonth(oneMonthLater.getMonth() + 1);

// if the next month has fewer days than current month, set to last day of the month
if (oneMonthLater.getDate() !== today.getDate()) {
    oneMonthLater.setDate(0);
}

const formatDate = (date) => date.toISOString().split('T')[0];

dateInput.min = formatDate(today); // sets minimum date to today
dateInput.max = formatDate(oneMonthLater); // sets maximum date to one month later
const submitBtn = document.getElementById('submit-btn');

// event listener for date input change
dateInput.addEventListener('change', fetchTimes);
timeSelect.addEventListener('change', () => submitBtn.classList.remove('disabled'));

function fetchTimes() {
    const serviceId = document.getElementById('sid-input').value;
    const date = dateInput.value;

    fetch(`/api/general/available-slots?sid=${serviceId}&d=${date}`)
        .then(response => response.json())
        .then(times => {
            submitBtn.classList.add('disabled');
            if (!Array.isArray(times) || times.length === 0) { // check if there are available times
                timeSelect.innerHTML = '<option value="" disabled selected>No available times</option>';
                return;
            }
            timeSelect.innerHTML = '<option value="" disabled selected>Select time</option>';
            times.forEach(time => {
                const option = document.createElement('option');
                option.value = time;
                option.textContent = time.substring(0, 5); // format time to HH:mm
                timeSelect.appendChild(option);
            });
        });

}


