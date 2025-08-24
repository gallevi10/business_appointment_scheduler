const MAX_FILE_SIZE_MB = 5; // maximum file size in megabytes
const fileInputs = document.querySelectorAll('input[type="file"]');

fileInputs.forEach(fileInput => {
    fileInput.addEventListener('change', function () {
        const file = this.files[0];
        if (file) {
            const fileSizeInMB = file.size / (1024 * 1024); // converts bytes to megabytes
            if (fileSizeInMB > MAX_FILE_SIZE_MB) {
                alert(`File size exceeds the maximum limit of ${MAX_FILE_SIZE_MB} MB. Please choose a smaller file.`);
                this.value = ''; // Clear the input
            }
        }
    });
})