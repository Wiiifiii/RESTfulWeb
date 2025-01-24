// scripts.js

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
            // Redirect to the delete URL which performs the deletion via GET
            window.location.href = deleteUrl;

            // If using POST deletion endpoints with forms, uncomment below:
            /*
            fetch(deleteUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': getCsrfToken() // Ensure CSRF token is correctly fetched
                },
                body: JSON.stringify({ _method: 'DELETE' }),
                credentials: 'include'
            })
            .then(response => {
                if (response.ok) {
                    Swal.fire({
                        title: 'Deleted!',
                        text: `${itemType} has been deleted.`,
                        icon: 'success',
                        timer: 2000,
                        showConfirmButton: false
                    }).then(() => window.location.reload());
                } else {
                    return response.json().then(data => {
                        throw new Error(data.message || 'Failed to delete the item.');
                    });
                }
            })
            .catch(error => {
                console.error('Error:', error);
                Swal.fire({
                    title: 'Error!',
                    text: error.message || 'An error occurred while deleting the item.',
                    icon: 'error'
                });
            });
            */
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

// Function to handle image viewing in modal (if applicable)
function setupViewButtons() {
    const viewButtons = document.querySelectorAll('.view-button');
    const modalImage = document.getElementById('modalImage');

    viewButtons.forEach(button => {
        button.addEventListener('click', function () {
            const imageUrl = this.getAttribute('data-image-url');
            modalImage.setAttribute('src', imageUrl);
        });
    });

    // Clear the image src when modal is hidden
    const viewModal = document.getElementById('viewImageModal');
    if (viewModal) {
        viewModal.addEventListener('hidden.bs.modal', function () {
            modalImage.setAttribute('src', '');
        });
    }
}

// Function to handle real-time search (if applicable)
function setupSearch() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', function () {
            const searchTerm = this.value.toLowerCase();
            const cards = document.querySelectorAll('.image-grid .card, .table-responsive .table tbody tr');

            cards.forEach(card => {
                // For grid items (images)
                const cardTitle = card.querySelector('.card-title');
                const imgSrc = card.querySelector('img') ? card.querySelector('img').getAttribute('src') : '';
                const idMatch = imgSrc.match(/\/web\/images\/view\/(\d+)/);
                const id = idMatch ? idMatch[1] : '';

                // For table rows (locations, measurements)
                const tableRow = card.querySelector('td:first-child');
                const tableId = tableRow ? tableRow.textContent.toLowerCase() : '';
                const ownerOrDetails = tableRow ? tableRow.nextElementSibling.textContent.toLowerCase() : '';

                if (
                    (cardTitle && cardTitle.textContent.toLowerCase().includes(searchTerm)) ||
                    (id && id.includes(searchTerm)) ||
                    (ownerOrDetails && ownerOrDetails.includes(searchTerm))
                ) {
                    card.style.display = '';
                } else {
                    card.style.display = 'none';
                }
            });
        });
    }
}

// Function to handle success/error messages with SweetAlert2
function handleAlerts() {
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
}

// Initialize all functions on DOM load
document.addEventListener('DOMContentLoaded', function () {
    // Setup delete buttons
    const deleteButtons = document.querySelectorAll('.delete-button');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function () {
            const deleteUrl = this.getAttribute('data-delete-url');
            const itemType = this.getAttribute('data-item-type') || 'Item';
            const itemDetails = this.getAttribute('data-item-details') || 'this item';
            confirmDelete(deleteUrl, itemType, itemDetails);
        });
    });

    // Setup restore buttons
    const restoreButtons = document.querySelectorAll('.restore-button');
    restoreButtons.forEach(button => {
        button.addEventListener('click', function () {
            const restoreUrl = this.getAttribute('data-restore-url');
            const itemType = this.getAttribute('data-item-type') || 'Item';
            const itemDetails = this.getAttribute('data-item-details') || 'this item';
            confirmRestore(restoreUrl, itemType, itemDetails);
        });
    });

    // Setup view buttons
    setupViewButtons();

    // Setup search
    setupSearch();

    // Handle alerts
    handleAlerts();
});
