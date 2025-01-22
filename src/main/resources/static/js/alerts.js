// alerts.js

// We'll use SweetAlert2's built-in features for confirm and toast
// Make sure SweetAlert2 is loaded before this (we included it in layout.html)

document.addEventListener("DOMContentLoaded", () => {
  
    // 1) Confirmation before delete
    // We'll look for any links/buttons with class "confirm-delete"
    const deleteLinks = document.querySelectorAll(".confirm-delete");
    deleteLinks.forEach(link => {
      link.addEventListener("click", (e) => {
        e.preventDefault(); // prevent the default navigation
        const href = link.getAttribute("href");
  
        Swal.fire({
          title: "Are you sure?",
          text: "This action cannot be undone!",
          icon: "warning",
          showCancelButton: true,
          confirmButtonColor: "#10a37f",
          cancelButtonColor: "#d33",
          confirmButtonText: "Yes, delete it!"
        }).then((result) => {
          if (result.isConfirmed) {
            // proceed to the link
            window.location.href = href;
          }
        });
      });
    });
  
    // 2) Check if there's a success message or error message in the URL
    // E.g., if you redirect with "?success=LocationCreated", you can parse that
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has("success")) {
      showToast("success", urlParams.get("success")); 
    }
    if (urlParams.has("error")) {
      showToast("error", urlParams.get("error"));
    }
  
    function showToast(type, message) {
      const Toast = Swal.mixin({
        toast: true,
        position: "top-end",
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true,
        background: "#40414f",
        color: "#fff"
      });
  
      Toast.fire({
        icon: type,
        title: message
      });
    }
  });
  