document.addEventListener('DOMContentLoaded', function () {
    const Swal = window.Swal;

    // Confirmation dialogs
    function confirmAction(title, html, icon, confirmText, confirmColor, cancelColor, actionUrl) {
        Swal.fire({
            title: title,
            html: html,
            icon: icon,
            showCancelButton: true,
            confirmButtonColor: confirmColor,
            cancelButtonColor: cancelColor,
            confirmButtonText: confirmText
        }).then((result) => {
            if (result.isConfirmed) {
                window.location.href = actionUrl;
            }
        });
    }

    function confirmDelete(deleteUrl, itemType, itemDetails) {
        confirmAction(
            `Delete ${itemType}?`,
            `Are you sure you want to delete <strong>${itemDetails}</strong>?`,
            'warning',
            'Yes, delete it!',
            '#dc3545',
            '#6c757d',
            deleteUrl
        );
    }

    function confirmRestore(restoreUrl, itemType, itemDetails) {
        confirmAction(
            `Restore ${itemType}?`,
            `Are you sure you want to restore <strong>${itemDetails}</strong>?`,
            'question',
            'Yes, restore it!',
            '#28a745',
            '#6c757d',
            restoreUrl
        );
    }

    function confirmDeletePermanent(deleteUrl, itemType, itemDetails) {
        confirmAction(
            `Permanently Delete ${itemType}?`,
            `Are you sure you want to permanently delete <strong>${itemDetails}</strong>? This action cannot be undone.`,
            'warning',
            'Yes, permanently delete it!',
            '#dc3545',
            '#6c757d',
            deleteUrl
        );
    }

    // Initialize button actions
    document.querySelectorAll('.delete-button').forEach(button => {
        button.addEventListener('click', function () {
            confirmDelete(this.dataset.deleteUrl, 'Image', this.dataset.itemDetails || 'this item');
        });
    });

    document.querySelectorAll('.restore-button').forEach(button => {
        button.addEventListener('click', function () {
            confirmRestore(this.dataset.restoreUrl, 'Image', this.dataset.itemDetails || 'this item');
        });
    });

    document.querySelectorAll('.delete-permanent-button').forEach(button => {
        button.addEventListener('click', function () {
            confirmDeletePermanent(this.dataset.deleteUrl, 'Image', this.dataset.itemDetails || 'this item');
        });
    });

    // Handle View Image modal
    document.querySelectorAll('.view-button').forEach(button => {
        button.addEventListener('click', function () {
            const imageUrl = this.dataset.imageUrl;
            document.getElementById('modalImage').setAttribute('src', imageUrl);
        });
    });

    // Flash messages
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
});
