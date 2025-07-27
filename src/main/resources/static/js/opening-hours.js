const modelEditRangeLink = document.getElementById('model-edit-range');
const modelDeleteRangeLink = document.getElementById('model-delete-range');

document.addEventListener('DOMContentLoaded', function () {
    const rangeLinks = document.querySelectorAll('.range-link');

    rangeLinks.forEach(link => {
        link.addEventListener('click', function () {
            let businessHourId = this.dataset.businesshourid;
            let isOnlyOneRange = this.dataset.isonlyonerange === 'true';
            modelEditRangeLink.href = `opening-hours/edit-range?bhid=${businessHourId}`;
            if (isOnlyOneRange) {
                modelDeleteRangeLink.style.display = 'none';
            }
            else {
                modelDeleteRangeLink.style.display = 'inline';
                modelDeleteRangeLink.href = `opening-hours/delete-range?bhid=${businessHourId}`;
            }
        });
    });
});
