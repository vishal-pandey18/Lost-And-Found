// Maps an axios error to a readable string. The backend's GlobalExceptionHandler
// always returns { status, error, message, details } - this reads that shape,
// but degrades gracefully for network failures, timeouts, and anything
// unexpected instead of showing "undefined" or a raw stack trace.
export function getErrorMessage(error) {
  if (!error.response) {
    if (error.code === "ECONNABORTED") {
      return "The request timed out. Please try again.";
    }
    return "Can't reach the server. Check your connection and try again.";
  }

  const { status, data } = error.response;

  if (data?.details?.length) {
    return data.details.join(" ");
  }
  if (data?.message) {
    return data.message;
  }

  switch (status) {
    case 400:
      return "That request wasn't valid. Please check the details and try again.";
    case 401:
      return "Your session has expired. Please log in again.";
    case 403:
      return "You don't have permission to do that.";
    case 404:
      return "That couldn't be found.";
    case 409:
      return "That already exists or conflicts with something.";
    case 422:
      return "Some of the information provided isn't valid.";
    case 429:
      return "Too many requests - please slow down and try again shortly.";
    case 500:
      return "Something went wrong on our end. Please try again.";
    default:
      return "Something unexpected happened. Please try again.";
  }
}
