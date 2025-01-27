// scripts.js

document.addEventListener('DOMContentLoaded', function () {
    // Initialize SweetAlert2
    const Swal = window.Swal;

    // Function to handle delete confirmation
    function confirmDelete(deleteUrl, itemType, itemDetails) {
        Swal.fire({
            title: `Delete ${itemType}?`,
            html: `Are you sure you want to delete <strong>${itemDetails}</strong>?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc3545',
            cancelButtonColor: '#6c757d',
            confirmButtonText: 'Yes, delete it!'
        }).then((result) => {
            if (result.isConfirmed) {
                window.location.href = deleteUrl;
            }
        });
    }

    // Function to handle restore confirmation
    function confirmRestore(restoreUrl, itemType, itemDetails) {
        Swal.fire({
            title: `Restore ${itemType}?`,
            html: `Are you sure you want to restore <strong>${itemDetails}</strong>?`,
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#28a745',
            cancelButtonColor: '#6c757d',
            confirmButtonText: 'Yes, restore it!'
        }).then((result) => {
            if (result.isConfirmed) {
                window.location.href = restoreUrl;
            }
        });
    }

    // Function to handle permanently delete confirmation
    function confirmDeletePermanent(deleteUrl, itemType, itemDetails) {
        Swal.fire({
            title: `Permanently Delete ${itemType}?`,
            html: `Are you sure you want to permanently delete <strong>${itemDetails}</strong>? This action cannot be undone.`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc3545',
            cancelButtonColor: '#6c757d',
            confirmButtonText: 'Yes, permanently delete it!'
        }).then((result) => {
            if (result.isConfirmed) {
                window.location.href = deleteUrl;
            }
        });
    }

    // Handle Delete Buttons
    const deleteButtons = document.querySelectorAll('.delete-button');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function () {
            const deleteUrl = this.getAttribute('data-delete-url');
            const itemType = this.getAttribute('data-item-type') || 'Item';
            const itemDetails = this.getAttribute('data-item-details') || 'this item';
            confirmDelete(deleteUrl, itemType, itemDetails);
        });
    });

    // Handle Restore Buttons
    const restoreButtons = document.querySelectorAll('.restore-button');
    restoreButtons.forEach(button => {
        button.addEventListener('click', function () {
            const restoreUrl = this.getAttribute('data-restore-url');
            const itemType = this.getAttribute('data-item-type') || 'Item';
            const itemDetails = this.getAttribute('data-item-details') || 'this item';
            confirmRestore(restoreUrl, itemType, itemDetails);
        });
    });

    // Handle Permanently Delete Buttons (Admin Only)
    const deletePermanentButtons = document.querySelectorAll('.delete-permanent-button');
    deletePermanentButtons.forEach(button => {
        button.addEventListener('click', function () {
            const deleteUrl = this.getAttribute('data-delete-url');
            const itemType = this.getAttribute('data-item-type') || 'Item';
            const itemDetails = this.getAttribute('data-item-details') || 'this item';
            confirmDeletePermanent(deleteUrl, itemType, itemDetails);
        });
    });

    // Handle Alerts from URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('success')) {
        Swal.fire({
            title: 'Success!',
            text: urlParams.get('success'),
            icon: 'success',
            timer: 3000,
            showConfirmButton: false
        });
    }
    if (urlParams.has('error')) {
        Swal.fire({
            title: 'Error!',
            text: urlParams.get('error'),
            icon: 'error',
            timer: 3000,
            showConfirmButton: false
        });
    }

    // Initialize Flatpickr if the library is loaded
    if (typeof flatpickr !== 'undefined') {
        flatpickr("#startDate", {
            enableTime: true,
            dateFormat: "Y-m-d\\TH:i:S",
        });

        flatpickr("#endDate", {
            enableTime: true,
            dateFormat: "Y-m-d\\TH:i:S",
        });
    }
});
