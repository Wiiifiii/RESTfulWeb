document.addEventListener('DOMContentLoaded', function () {
  // Handle soft delete
  const deleteButtons = document.querySelectorAll('.delete-button');
  deleteButtons.forEach(button => {
      button.addEventListener('click', function () {
          const deleteUrl = this.getAttribute('data-delete-url');
          const itemType = this.getAttribute('data-item-type');
          const itemDetails = this.getAttribute('data-item-details');

          Swal.fire({
              title: 'Are you sure?',
              text: `Do you want to delete the ${itemType}: ${itemDetails}?`,
              icon: 'warning',
              showCancelButton: true,
              confirmButtonText: 'Yes, delete it!',
              cancelButtonText: 'Cancel'
          }).then((result) => {
              if (result.isConfirmed) {
                  window.location.href = deleteUrl;
              }
          });
      });
  });

  // Handle restore
  const restoreButtons = document.querySelectorAll('.restore-button');
  restoreButtons.forEach(button => {
      button.addEventListener('click', function () {
          const restoreUrl = this.getAttribute('data-restore-url');
          const itemType = this.getAttribute('data-item-type');
          const itemDetails = this.getAttribute('data-item-details');

          Swal.fire({
              title: 'Are you sure?',
              text: `Do you want to restore the ${itemType}: ${itemDetails}?`,
              icon: 'question',
              showCancelButton: true,
              confirmButtonText: 'Yes, restore it!',
              cancelButtonText: 'Cancel'
          }).then((result) => {
              if (result.isConfirmed) {
                  // Create a form to submit POST request
                  const form = document.createElement('form');
                  form.method = 'POST';
                  form.action = restoreUrl;
                  document.body.appendChild(form);
                  form.submit();
              }
          });
      });
  });

  // Handle permanent delete
  const deletePermanentButtons = document.querySelectorAll('.delete-permanent-button');
  deletePermanentButtons.forEach(button => {
      button.addEventListener('click', function () {
          const deleteUrl = this.getAttribute('data-delete-url');
          const itemType = this.getAttribute('data-item-type');
          const itemDetails = this.getAttribute('data-item-details');

          Swal.fire({
              title: 'Are you absolutely sure?',
              text: `This action will permanently delete the ${itemType}: ${itemDetails}. This cannot be undone!`,
              icon: 'warning',
              showCancelButton: true,
              confirmButtonText: 'Yes, permanently delete it!',
              cancelButtonText: 'Cancel'
          }).then((result) => {
              if (result.isConfirmed) {
                  // Create a form to submit POST request
                  const form = document.createElement('form');
                  form.method = 'POST'; // Use POST with hidden _method=DELETE if needed
                  form.action = deleteUrl;

                  // CSRF token if required
                  const csrfToken = document.querySelector('input[name="_csrf"]').value;
                  const csrfInput = document.createElement('input');
                  csrfInput.type = 'hidden';
                  csrfInput.name = '_csrf';
                  csrfInput.value = csrfToken;
                  form.appendChild(csrfInput);

                  document.body.appendChild(form);
                  form.submit();
              }
          });
      });
  });
});
