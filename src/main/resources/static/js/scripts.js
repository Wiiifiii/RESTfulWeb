// File: src/main/resources/static/js/scripts.js

document.addEventListener('DOMContentLoaded', function() {

  /**
   * Show SweetAlert2 dialog, then do either:
   *  - GET => window.location.href
   *  - POST => dynamically create <form method="POST">
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
          const form = document.createElement('form');
          form.method = 'POST';
          form.action = actionUrl;

          // Include CSRF token if present
          const csrfToken = document.querySelector('input[name="_csrf"]');
          if (csrfToken) {
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = csrfToken.name;
            csrfInput.value = csrfToken.value;
            form.appendChild(csrfInput);
          }

          document.body.appendChild(form);
          form.submit();
        } else {
          // Default: GET
          window.location.href = actionUrl;
        }
      }
    });
  }

  // Soft delete => GET
  function confirmDelete(deleteUrl, itemType, itemDetails) {
    confirmAction({
      title: `Delete ${itemType}?`,
      html: `Are you sure you want to delete <strong>${itemDetails}</strong>?`,
      icon: 'warning',
      confirmText: 'Yes, delete it!',
      confirmColor: '#dc3545', // red
      cancelColor: '#6c757d', // gray
      actionUrl: deleteUrl,
      method: 'GET'
    });
  }

  // Restore => POST
  function confirmRestore(restoreUrl, itemType, itemDetails) {
    confirmAction({
      title: `Restore ${itemType}?`,
      html: `Are you sure you want to restore <strong>${itemDetails}</strong>?`,
      icon: 'question',
      confirmText: 'Yes, restore it!',
      confirmColor: '#28a745', // green
      cancelColor: '#6c757d', // gray
      actionUrl: restoreUrl,
      method: 'POST'
    });
  }

  // Permanently delete => POST
  function confirmDeletePermanent(deleteUrl, itemType, itemDetails) {
    confirmAction({
      title: `Permanently Delete ${itemType}?`,
      html: `Permanently remove <strong>${itemDetails}</strong>?<br>This cannot be undone.`,
      icon: 'warning',
      confirmText: 'Yes, permanently delete!',
      confirmColor: '#dc3545', // red
      cancelColor: '#6c757d', // gray
      actionUrl: deleteUrl,
      method: 'POST'
    });
  }

  // Hook up event listeners for each button
  document.querySelectorAll('.delete-button').forEach(button => {
    button.addEventListener('click', function() {
      const deleteUrl = this.dataset.deleteUrl;
      const itemType = this.dataset.itemType || 'Item';
      const itemDetails = this.dataset.itemDetails || 'this item';
      confirmDelete(deleteUrl, itemType, itemDetails);
    });
  });

  document.querySelectorAll('.restore-button').forEach(button => {
    button.addEventListener('click', function() {
      const restoreUrl = this.dataset.restoreUrl;
      const itemType = this.dataset.itemType || 'Item';
      const itemDetails = this.dataset.itemDetails || 'this item';
      confirmRestore(restoreUrl, itemType, itemDetails);
    });
  });

  document.querySelectorAll('.delete-permanent-button').forEach(button => {
    button.addEventListener('click', function() {
      const deleteUrl = this.dataset.deleteUrl;
      const itemType = this.dataset.itemType || 'Item';
      const itemDetails = this.dataset.itemDetails || 'this item';
      confirmDeletePermanent(deleteUrl, itemType, itemDetails);
    });
  });
});
