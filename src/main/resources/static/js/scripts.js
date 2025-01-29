// File: src/main/resources/static/js/scripts.js

document.addEventListener('DOMContentLoaded', function() {

  /**
   * Helper to show a SweetAlert2 dialog and either do:
   *  - GET navigation (window.location.href)
   *  - or create a dynamic <form method="POST"> to submit to the server
   */
  function confirmAction({
    title,
    html,
    icon,
    confirmText,
    confirmColor,
    cancelColor,
    actionUrl,
    method = 'GET'
  }) {
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
        if (method.toUpperCase() === 'POST') {
          // Create a form dynamically, submit as POST
          const form = document.createElement('form');
          form.method = 'POST';
          form.action = actionUrl;
          // If CSRF is needed, you can inject a hidden input here as well
          document.body.appendChild(form);
          form.submit();
        } else {
          // Default: GET
          window.location.href = actionUrl;
        }
      }
    });
  }

  function confirmDelete(deleteUrl, itemType, itemDetails) {
    confirmAction({
      title: `Delete ${itemType}?`,
      html: `Are you sure you want to delete <strong>${itemDetails}</strong>?`,
      icon: 'warning',
      confirmText: 'Yes, delete it!',
      confirmColor: '#dc3545', // red
      cancelColor: '#6c757d', // gray
      actionUrl: deleteUrl,
      method: 'GET' // If your soft-delete endpoint is GET
    });
  }

  function confirmRestore(restoreUrl, itemType, itemDetails) {
    confirmAction({
      title: `Restore ${itemType}?`,
      html: `Are you sure you want to restore <strong>${itemDetails}</strong>?`,
      icon: 'question',
      confirmText: 'Yes, restore it!',
      confirmColor: '#28a745', // green
      cancelColor: '#6c757d', // gray
      actionUrl: restoreUrl,
      method: 'POST' // If your restore endpoint is POST
    });
  }

  function confirmDeletePermanent(deleteUrl, itemType, itemDetails) {
    confirmAction({
      title: `Permanently Delete ${itemType}?`,
      html: `Permanently remove <strong>${itemDetails}</strong>?<br>This cannot be undone.`,
      icon: 'warning',
      confirmText: 'Yes, permanently delete!',
      confirmColor: '#dc3545',
      cancelColor: '#6c757d',
      actionUrl: deleteUrl,
      method: 'POST' // If your permanent-delete endpoint is POST
    });
  }

  // Attach to .delete-button
  document.querySelectorAll('.delete-button').forEach(button => {
    button.addEventListener('click', function() {
      const deleteUrl = this.dataset.deleteUrl;
      const itemType = this.dataset.itemType || 'Item';
      const itemDetails = this.dataset.itemDetails || 'this item';
      confirmDelete(deleteUrl, itemType, itemDetails);
    });
  });

  // Attach to .restore-button
  document.querySelectorAll('.restore-button').forEach(button => {
    button.addEventListener('click', function() {
      const restoreUrl = this.dataset.restoreUrl;
      const itemType = this.dataset.itemType || 'Item';
      const itemDetails = this.dataset.itemDetails || 'this item';
      confirmRestore(restoreUrl, itemType, itemDetails);
    });
  });

  // Attach to .delete-permanent-button
  document.querySelectorAll('.delete-permanent-button').forEach(button => {
    button.addEventListener('click', function() {
      const deleteUrl = this.dataset.deleteUrl;
      const itemType = this.dataset.itemType || 'Item';
      const itemDetails = this.dataset.itemDetails || 'this item';
      confirmDeletePermanent(deleteUrl, itemType, itemDetails);
    });
  });

});

document.addEventListener('DOMContentLoaded', function() {

  function confirmDelete(url, itemType, itemDetails) {
    // show SweetAlert, if confirmed => window.location = url;
  }

  document.querySelectorAll('.delete-button').forEach(btn => {
    btn.addEventListener('click', () => {
      const url = btn.dataset.deleteUrl;
      const itemType = btn.dataset.itemType;
      const itemDetails = btn.dataset.itemDetails;
      confirmDelete(url, itemType, itemDetails);
    });
  });

  // similarly for .restore-button and .delete-permanent-button
});

