

#### Fetch all events for an issue like this 

```python
issue = "12345678"
events = get_all_issue_events(issue)
```

* First set your `SENTRY_AUTH_TOKEN` as env var
* With definitions..

```python
import requests
import time

def get_all_issue_events(organization_slug, project_slug, issue_id):
    url = 'https://app.getsentry.com/api/0/issues/%s/events/' % issue_id
    all_data = get_all_data(url)
    event_ids = [x["id"] for x in all_data]
    detail_vec = [get_event_data(organization_slug, project_slug, event_id) for event_id in event_ids]
    return all_data, detail_vec


def get_all_data(url):
    token = os.environ.get('SENTRY_AUTH_TOKEN')
    headers = {"Authorization": "Bearer %s" % token,
            'Content-Type': 'application/json'}

    next_results = 'true'
    next_url = url

    all_data = []
    while next_results == 'true':

        # Do fetch
        pass
        response = requests.get(next_url, headers=headers)

        if response.status_code == 200:
            data = response.json()
            if isinstance(data, list):
                all_data += data
            elif isinstance(data, dict):
                all_data.append(data)

            next_results = response.links.get('next', {}).get('results')
            next_url = response.links.get('next', {}).get('url')
            time.sleep(0.4)
        else:
            next_results = 'false'

    return all_data
    
def get_event_data(organization_slug, project_slug, event_id):

    url = f"https://sentry.io/api/0/projects/{organization_slug}/{project_slug}/events/{event_id}/"
    
    token = os.environ.get('SENTRY_AUTH_TOKEN')
    headers = {"Authorization": "Bearer %s" % token,
            'Content-Type': 'application/json'}
    response = requests.get(url, headers=headers)
    return response.json()
    

```
