document.addEventListener('DOMContentLoaded', function () {
    const Swal = window.Swal;

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

    document.querySelectorAll('.view-button').forEach(button => {
        button.addEventListener('click', function () {
            const imageUrl = this.dataset.imageUrl;
            document.getElementById('modalImage').setAttribute('src', imageUrl);
        });
    });

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
document.addEventListener('DOMContentLoaded', function () {
    const Swal = window.Swal;

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

    // Attach event listeners to any .delete-button, .restore-button, etc.
    document.querySelectorAll('.delete-button').forEach(button => {
        button.addEventListener('click', function () {
            const deleteUrl = this.dataset.deleteUrl;
            const itemType = this.dataset.itemType || 'Item';
            const itemDetails = this.dataset.itemDetails || 'this item';
            confirmDelete(deleteUrl, itemType, itemDetails);
        });
    });

    document.querySelectorAll('.restore-button').forEach(button => {
        button.addEventListener('click', function () {
            const restoreUrl = this.dataset.restoreUrl;
            const itemType = this.dataset.itemType || 'Item';
            const itemDetails = this.dataset.itemDetails || 'this item';
            confirmRestore(restoreUrl, itemType, itemDetails);
        });
    });

    document.querySelectorAll('.delete-permanent-button').forEach(button => {
        button.addEventListener('click', function () {
            const deleteUrl = this.dataset.deleteUrl;
            const itemType = this.dataset.itemType || 'Item';
            const itemDetails = this.dataset.itemDetails || 'this item';
            confirmDeletePermanent(deleteUrl, itemType, itemDetails);
        });
    });
});
document.addEventListener('DOMContentLoaded', function () {
    const Swal = window.Swal;

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

    // .delete-button => data-delete-url
    document.querySelectorAll('.delete-button').forEach(button => {
        button.addEventListener('click', function () {
            const deleteUrl = this.dataset.deleteUrl;
            const itemType = this.dataset.itemType || 'Item';
            const itemDetails = this.dataset.itemDetails || 'this item';
            confirmDelete(deleteUrl, itemType, itemDetails);
        });
    });

    // .restore-button => data-restore-url
    document.querySelectorAll('.restore-button').forEach(button => {
        button.addEventListener('click', function () {
            const restoreUrl = this.dataset.restoreUrl;
            const itemType = this.dataset.itemType || 'Item';
            const itemDetails = this.dataset.itemDetails || 'this item';
            confirmRestore(restoreUrl, itemType, itemDetails);
        });
    });

    // .delete-permanent-button => data-delete-url
    document.querySelectorAll('.delete-permanent-button').forEach(button => {
        button.addEventListener('click', function () {
            const deleteUrl = this.dataset.deleteUrl;
            const itemType = this.dataset.itemType || 'Item';
            const itemDetails = this.dataset.itemDetails || 'this item';
            confirmDeletePermanent(deleteUrl, itemType, itemDetails);
        });
    });
});
