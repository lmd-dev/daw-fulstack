class SSESubscriptionException extends Error
{
    constructor(channel)
    {
        super(`Unable to subscribe to '${channel}' channel`);
    }
}

class SSEUnsubscriptionException extends Error
{
    constructor(channel)
    {
        super(`Unable to unsubscribe from '${channel}' channel`);
    }
}

export const SSEEvent = {
    CONNECT: "open",
    CONNECTION_ERROR: "connection-error",
    CONNECTION_LOST: "connection-lost"
}

export class SSEClient extends EventTarget
{
    //Remote SSE Server Base URL 
    #baseUrl;

    //SSE Client Id
    #clientId;

    //SSE link with the remote server
    #eventSource;

    //List of subscribed channels
    #subscribedChannels;

    /**
     * SSEClient constructor
     * @param {string} baseUrl: Remote SSE Server Base URL 
     */
    constructor(baseUrl)
    {
        super();
        
        this.baseUrl = baseUrl;
        this.clientId = this.#loadClientId();

        this.eventSource = null;
        this.#subscribedChannels = new Map();
    }

    /**
     * Tries to load SSE Client Id from localStorage or generate a new one
     * @returns {string} Current SSE Client Id
     */
    #loadClientId()
    {
        let uuid = sessionStorage.getItem(`sse-id-${this.baseUrl}`);

        if (uuid == null)
        {
            uuid = this.#generateClientId();

            this.#saveClientId(uuid);
        }

        return uuid;
    }

    /**
     * Saves current SSE Client Id into localStorage
     * @param {string} clientId 
     */
    #saveClientId(clientId)
    {
        sessionStorage.setItem(`sse-id-${this.baseUrl}`, clientId);
    }

    /**
     * Generates a new SSE Client Id from UUID string
     * @returns {string} New SSE Client Id
     */
    #generateClientId()
    {
        return crypto.randomUUID();
    }

    /**
     * Tries to connect to open a SSE connection with the server
     * @returns {Promise<void>}
     */
    async connect()
    {
        return new Promise((resolve, reject) =>
        {
            if (this.eventSource !== null)
            {
                resolve();
                return;
            }

            this.eventSource = new EventSource(`//${this.baseUrl}/__sse/${this.clientId}`, {});

            this.eventSource.onopen = () =>
            {
                this.eventSource.onerror = (error) => { this.dispatchEvent(new Event(SSEEvent.CONNECTION_LOST)) };

                this.#renewSubscriptions();

                this.dispatchEvent(new Event(SSEEvent.CONNECT));

                resolve();
            };

            this.eventSource.onerror = () =>
            {
                this.eventSource.onerror = (error) => { this.dispatchEvent(new Event(SSEEvent.CONNECTION_ERROR)) };
                reject();
            }
        })
    }

    /**
     * Closes the SSE connection with the server
     */
    async disconnect()
    {
        if (this.eventSource)
        {
            this.eventSource.close();
            this.eventSource = null;
        }
    }

    /**
     * Tries to subscribe to a channel
     * @param {string} channel 
     * @param {(any) => void} callback 
     * @returns {Promise<boolean>}
     */
    async subscribe(channel, callback)
    {
        if (callback === undefined)
            return false;

        try
        {
            await this.#askForSubscription(channel);

            this.#memorizeChannel(channel, callback);

            this.#attachEventListener(channel, callback);

            return true;
        }
        catch (error)
        {
            return false;
        }
    }

    /**
     * Tries to unsubscribe from a server channel
     * @param {string} channel 
     */
    async unsubscribe(channel)
    {
        const response = await fetch(`//${this.baseUrl}/__sse/${this.clientId}/channel/${channel}`, { method: "delete" });

        if (response.status !== 200)
            throw new SSEUnsubscriptionException(channel);

        this.#forgetChannel(channel);
    }

    /**
     * Tries 
     * @param {*} channel 
     */
    async #askForSubscription(channel)
    {
        const response = await fetch(`//${this.baseUrl}/__sse/${this.clientId}/channel/${channel}`, { method: "post" });

        if (response.status !== 200)
            throw new SSESubscriptionException(channel);
    }

    async #renewSubscriptions()
    {
        for (const channel of this.#subscribedChannels.keys())
        {
            await this.#askForSubscription(channel);
        }
    }

    #memorizeChannel(channel, callback)
    {
        if (this.#subscribedChannels.has(channel) == false)
            this.#subscribedChannels.set(channel, callback);
    }

    #forgetChannel(channel)
    {
        if (this.#subscribedChannels.has(channel))
            this.#subscribedChannels.delete(channel);
    }

    #attachEventListener(channel, callback)
    {
        this.eventSource.addEventListener(channel, (event) =>
        {
            let data;

            try
            {
                data = JSON.parse(event.data);
            }
            catch (e)
            {
                data = {};
            }

            callback?.(data);
        });
    }
}