document.addEventListener('DOMContentLoaded', function () {
    // Reference to SweetAlert2
    const Swal = window.Swal;

    /**
     * Generic helper for SweetAlert2 confirmation dialogs.
     */
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

    /**
     * Specific wrappers for Delete, Restore, and Permanent Delete.
     */
    function confirmDelete(deleteUrl, itemType, itemDetails) {
        confirmAction(
            `Delete ${itemType}?`,
            `Are you sure you want to delete <strong>${itemDetails}</strong>?`,
            'warning',
            'Yes, delete it!',
            '#dc3545', // confirmColor (red)
            '#6c757d', // cancelColor (gray)
            deleteUrl
        );
    }

    function confirmRestore(restoreUrl, itemType, itemDetails) {
        confirmAction(
            `Restore ${itemType}?`,
            `Are you sure you want to restore <strong>${itemDetails}</strong>?`,
            'question',
            'Yes, restore it!',
            '#28a745', // confirmColor (green)
            '#6c757d', // cancelColor (gray)
            restoreUrl
        );
    }

    function confirmDeletePermanent(deleteUrl, itemType, itemDetails) {
        confirmAction(
            `Permanently Delete ${itemType}?`,
            `Are you sure you want to permanently delete <strong>${itemDetails}</strong>? 
             This action cannot be undone.`,
            'warning',
            'Yes, permanently delete it!',
            '#dc3545', // confirmColor (red)
            '#6c757d', // cancelColor (gray)
            deleteUrl
        );
    }

    /**
     * Attach event listeners for delete/restore/permanent-delete buttons.
     * These rely on data-* attributes (data-delete-url, data-restore-url, data-item-type, data-item-details).
     */
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

    /**
     * If you have a "View" button that should open a file or image, attach here.
     * For example, if .view-button has data-image-url or data-file-url.
     * 
     * Example approach: open in new tab/window.
     */
    document.querySelectorAll('.view-button').forEach(button => {
        button.addEventListener('click', function () {
            const fileUrl = this.dataset.imageUrl || this.dataset.fileUrl;
            if (fileUrl) {
                // Open in new tab
                window.open(fileUrl, '_blank');
            } else {
                // Or show a modal—whatever logic you want.
                Swal.fire('No file URL provided.', '', 'info');
            }
        });
    });

    /**
     * Show sweetalert for "success" or "error" messages if present in the URL (e.g. ?success=...).
     */
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
