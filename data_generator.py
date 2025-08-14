import string

chars = string.ascii_lowercase + string.ascii_uppercase + string.digits

print(r'data: {"choices":[],"created":0,"id":"","model":"","object":"","prompt_filter_results":[{"prompt_index":0,"content_filter_results":{"hate":{"filtered":false,"severity":"safe"},"jailbreak":{"filtered":false,"detected":false},"self_harm":{"filtered":false,"severity":"safe"},"sexual":{"filtered":false,"severity":"safe"},"violence":{"filtered":false,"severity":"safe"}}}]}')
print()

# Loop for 1000 rounds, cycling through characters
for i in range(1000):
    char = chars[i % len(chars)]
    print(r'data: {"choices":[{"content_filter_results":{"hate":{"filtered":false,"severity":"safe"},"protected_material_code":{"filtered":false,"detected":false},"protected_material_text":{"filtered":false,"detected":false},"self_harm":{"filtered":false,"severity":"safe"},"sexual":{"filtered":false,"severity":"safe"},"violence":{"filtered":false,"severity":"safe"}},"delta":{"content":"' + char + r'"},"finish_reason":null,"index":0,"logprobs":null}],"created":1754997500,"id":"chatcmpl-AAAAAAAAAAbbbbbbbbbb123456789","model":"gpt-4o-2024-08-06","object":"chat.completion.chunk","system_fingerprint":"fp_abcdef1234"}')
    print()

print(r'data: [DONE]')
