// scripts.js

document.addEventListener('DOMContentLoaded', function () {
    // Function to get CSRF token and header from meta tags
    function getCsrfToken() {
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
        return { csrfToken, csrfHeader };
    }

    // Handle Soft Delete Buttons
    const deleteButtons = document.querySelectorAll('.delete-button');
    deleteButtons.forEach(function(button) {
        button.addEventListener('click', function() {
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
                    // Perform the delete operation via AJAX POST request
                    const { csrfToken, csrfHeader } = getCsrfToken();

                    fetch(deleteUrl, {
                        method: 'POST', // Changed to POST
                        headers: {
                            [csrfHeader]: csrfToken,
                            'Content-Type': 'application/x-www-form-urlencoded'
                        },
                        credentials: 'same-origin' // Include cookies
                    })
                    .then(response => {
                        if (response.redirected) {
                            // If redirected, navigate to the new URL
                            window.location.href = response.url;
                        } else if (response.ok) {
                            Swal.fire(
                                'Deleted!',
                                'The file has been deleted.',
                                'success'
                            ).then(() => {
                                // Reload the page to reflect changes
                                window.location.reload();
                            });
                        } else {
                            return response.text().then(text => { throw new Error(text) });
                        }
                    })
                    .catch(error => {
                        Swal.fire(
                            'Error!',
                            `Failed to delete the file: ${error.message}`,
                            'error'
                        );
                    });
                }
            });
        });
    });

    // Handle Permanently Delete Buttons (ADMIN ONLY)
    const deletePermanentButtons = document.querySelectorAll('.delete-permanent-button');
    deletePermanentButtons.forEach(function(button) {
        button.addEventListener('click', function() {
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
                    // Perform the permanent delete via AJAX POST request
                    const { csrfToken, csrfHeader } = getCsrfToken();

                    fetch(deleteUrl, {
                        method: 'POST', // Changed from DELETE to POST for form compatibility
                        headers: {
                            [csrfHeader]: csrfToken,
                            'Content-Type': 'application/x-www-form-urlencoded'
                        },
                        credentials: 'same-origin' // Include cookies
                    })
                    .then(response => {
                        if (response.redirected) {
                            // If redirected, navigate to the new URL
                            window.location.href = response.url;
                        } else if (response.ok) {
                            Swal.fire(
                                'Permanently Deleted!',
                                'The file has been permanently deleted.',
                                'success'
                            ).then(() => {
                                // Reload the page to reflect changes
                                window.location.reload();
                            });
                        } else {
                            return response.text().then(text => { throw new Error(text) });
                        }
                    })
                    .catch(error => {
                        Swal.fire(
                            'Error!',
                            `Failed to permanently delete the file: ${error.message}`,
                            'error'
                        );
                    });
                }
            });
        });
    });

    // Handle Restore Buttons
    const restoreButtons = document.querySelectorAll('.restore-button');
    restoreButtons.forEach(function(button) {
        button.addEventListener('click', function() {
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
                    // Perform the restore operation via AJAX POST request
                    const { csrfToken, csrfHeader } = getCsrfToken();

                    fetch(restoreUrl, {
                        method: 'POST',
                        headers: {
                            [csrfHeader]: csrfToken,
                            'Content-Type': 'application/x-www-form-urlencoded'
                        },
                        credentials: 'same-origin' // Include cookies
                    })
                    .then(response => {
                        if (response.redirected) {
                            // If redirected, navigate to the new URL
                            window.location.href = response.url;
                        } else if (response.ok) {
                            Swal.fire(
                                'Restored!',
                                'The file has been restored.',
                                'success'
                            ).then(() => {
                                // Reload the page to reflect changes
                                window.location.reload();
                            });
                        } else {
                            return response.text().then(text => { throw new Error(text) });
                        }
                    })
                    .catch(error => {
                        Swal.fire(
                            'Error!',
                            `Failed to restore the file: ${error.message}`,
                            'error'
                        );
                    });
                }
            });
        });
    });
});
