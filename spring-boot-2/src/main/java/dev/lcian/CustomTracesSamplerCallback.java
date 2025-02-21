package dev.lcian;

import io.sentry.SamplingContext;
import io.sentry.SentryOptions.TracesSamplerCallback;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;

@Component
class CustomTracesSamplerCallback implements TracesSamplerCallback {
    @Override
    public Double sample(SamplingContext context) {
        HttpServletRequest request = (HttpServletRequest) context.getCustomSamplingContext().get("request");
        String url = request.getRequestURI();
        if ("/payment".equals(url)) {
            // These are important - take a big sample
            return 0.5;
        } else if ("/search".equals(url)) {
            // Search is less important and happen much more frequently - only take 1%
            return 0.01;
        } else if ("/health".equals(url)) {
            // The health check endpoint is just noise - drop all transactions
            return 0d;
        } else {
            // Default sample rate
            return 0.1;
        }
    }
}
