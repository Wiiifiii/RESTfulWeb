document.addEventListener('DOMContentLoaded', function () {
    // Function to get CSRF token and header from meta tags
    function getCsrfToken() {
        const csrfToken = document.querySelector('meta[name="_csrf"]')
            ? document.querySelector('meta[name="_csrf"]').getAttribute('content')
            : null;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')
            ? document.querySelector('meta[name="_csrf_header"]').getAttribute('content')
            : null;
        return { csrfToken, csrfHeader };
    }

    // ------------------------------
    // SweetAlert for Delete Buttons
    // ------------------------------
    const deleteButtons = document.querySelectorAll('.delete-button');
    deleteButtons.forEach(function (button) {
        button.addEventListener('click', function (e) {
            e.preventDefault();
            const deleteUrl = button.getAttribute('data-delete-url');
            const itemType = button.getAttribute('data-item-type');
            const itemDetails = button.getAttribute('data-item-details');

            Swal.fire({
                title: `Are you sure you want to delete this ${itemType}?`,
                text: `Item: ${itemDetails}`,
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Yes, delete it!',
                cancelButtonText: 'Cancel'
            }).then((result) => {
                if (result.isConfirmed) {
                    const { csrfToken, csrfHeader } = getCsrfToken();
                    fetch(deleteUrl, {
                        method: 'POST',
                        headers: {
                            ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {}),
                            'Content-Type': 'application/x-www-form-urlencoded'
                        },
                        credentials: 'same-origin'
                    })
                    .then(response => {
                        if (response.redirected) {
                            window.location.href = response.url;
                        } else if (response.ok) {
                            Swal.fire(
                                'Deleted!',
                                `The ${itemType.toLowerCase()} has been deleted.`,
                                'success'
                            ).then(() => {
                                window.location.href = '/web/images';
                            });
                        } else {
                            return response.text().then(text => { throw new Error(text) });
                        }
                    })
                    .catch(error => {
                        Swal.fire(
                            'Error!',
                            `Failed to delete the ${itemType.toLowerCase()}: ${error.message}`,
                            'error'
                        );
                    });
                }
            });
        });
    });

    // -------------------------------
    // SweetAlert for Restore Buttons
    // -------------------------------
    const restoreButtons = document.querySelectorAll('.restore-button');
    restoreButtons.forEach(function (button) {
        button.addEventListener('click', function (e) {
            e.preventDefault();
            const restoreUrl = button.getAttribute('data-restore-url');
            const itemType = button.getAttribute('data-item-type');
            const itemDetails = button.getAttribute('data-item-details');

            Swal.fire({
                title: `Are you sure you want to restore this ${itemType}?`,
                text: `Item: ${itemDetails}`,
                icon: 'question',
                showCancelButton: true,
                confirmButtonText: 'Yes, restore it!',
                cancelButtonText: 'Cancel'
            }).then((result) => {
                if (result.isConfirmed) {
                    const { csrfToken, csrfHeader } = getCsrfToken();
                    fetch(restoreUrl, {
                        method: 'POST',
                        headers: {
                            ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {}),
                            'Content-Type': 'application/x-www-form-urlencoded'
                        },
                        credentials: 'same-origin'
                    })
                    .then(response => {
                        if (response.redirected) {
                            window.location.href = response.url;
                        } else if (response.ok) {
                            Swal.fire(
                                'Restored!',
                                `The ${itemType.toLowerCase()} has been restored.`,
                                'success'
                            ).then(() => {
                                window.location.reload();
                            });
                        } else {
                            return response.text().then(text => { throw new Error(text) });
                        }
                    })
                    .catch(error => {
                        Swal.fire(
                            'Error!',
                            `Failed to restore the ${itemType.toLowerCase()}: ${error.message}`,
                            'error'
                        );
                    });
                }
            });
        });
    });

    // ---------------------------------------
    // SweetAlert for Permanently Delete Buttons (ADMIN ONLY)
    // ---------------------------------------
    const deletePermanentButtons = document.querySelectorAll('.delete-permanent-button');
    deletePermanentButtons.forEach(function (button) {
        button.addEventListener('click', function (e) {
            e.preventDefault();
            const deleteUrl = button.getAttribute('data-delete-url');
            const itemType = button.getAttribute('data-item-type');
            const itemDetails = button.getAttribute('data-item-details');

            Swal.fire({
                title: `Are you sure you want to permanently delete this ${itemType}?`,
                text: `Item: ${itemDetails}`,
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Yes, permanently delete it!',
                cancelButtonText: 'Cancel'
            }).then((result) => {
                if (result.isConfirmed) {
                    const { csrfToken, csrfHeader } = getCsrfToken();
                    fetch(deleteUrl, {
                        method: 'POST',
                        headers: {
                            ...(csrfHeader && csrfToken ? { [csrfHeader]: csrfToken } : {}),
                            'Content-Type': 'application/x-www-form-urlencoded'
                        },
                        credentials: 'same-origin'
                    })
                    .then(response => {
                        if (response.redirected) {
                            window.location.href = response.url;
                        } else if (response.ok) {
                            Swal.fire(
                                'Permanently Deleted!',
                                `The ${itemType.toLowerCase()} has been permanently deleted.`,
                                'success'
                            ).then(() => {
                                window.location.reload();
                            });
                        } else {
                            return response.text().then(text => { throw new Error(text) });
                        }
                    })
                    .catch(error => {
                        Swal.fire(
                            'Error!',
                            `Failed to permanently delete the ${itemType.toLowerCase()}: ${error.message}`,
                            'error'
                        );
                    });
                }
            });
        });
    });
});
